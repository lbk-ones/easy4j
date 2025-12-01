package easy4j.infra.rpc.utils;
import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class MetricsCollector {
    // Oshi 核心对象（初始化一次，线程安全）
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();
    private static final CentralProcessor CPU = HARDWARE.getProcessor();
    private static final GlobalMemory MEMORY = HARDWARE.getMemory();
    private static final FileSystem FILE_SYSTEM = OS.getFileSystem();

    // JVM 原生 API 对象
    private static final OperatingSystemMXBean JVM_OS_BEAN = 
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean JVM_MEM_BEAN = ManagementFactory.getMemoryMXBean();

    // 常量：单位转换（字节 -> MB/GB）
    private static final double BYTE_TO_MB = 1024.0 * 1024.0;
    private static final double BYTE_TO_GB = BYTE_TO_MB * 1024.0;

    // CPU 使用率计算：需要缓存上次的 CPU 时间片（保留这行代码，避免瞬时值不准）
    private static long[] PREVIOUS_CPU_TICKS = CPU.getSystemCpuLoadTicks();

    /**
     * 采集所有指标，填充 SystemMetrics 对象
     */
    public SystemMetrics collectAllMetrics() {
        SystemMetrics metrics = new SystemMetrics();

        // 1. 采集 CPU 指标（系统 + JVM 进程）
        collectCpuMetrics(metrics);
        // 2. 采集内存指标（系统 + JVM）
        collectMemoryMetrics(metrics);
        // 3. 采集磁盘指标（系统 + Pod）
        collectDiskMetrics(metrics);
        // 4. 采集 Pod 资源指标（K8s 环境下生效）
        if (isK8sEnvironment()) {
            collectPodMetrics(metrics);
        }

        return metrics;
    }

    // ------------------------------ CPU 指标采集 ------------------------------
    private void collectCpuMetrics(SystemMetrics metrics) {
        // 1. 系统 CPU 使用率（需对比两次 CPU 时间片，等待 100ms 保证准确性）
        double systemCpuLoad = CPU.getSystemCpuLoad(100); // 阻塞 100ms，获取平均负载
        metrics.setSystemCpuUsagePercentage(round(systemCpuLoad * 100, 2));

        // 2. JVM 进程 CPU 使用率（Java 7+ 支持，返回 0.0~N，N=CPU核心数）
        double jvmCpuLoad = JVM_OS_BEAN.getProcessCpuLoad();
        int cpuCoreCount = CPU.getLogicalProcessorCount();
        // 转换为百分比（如 8 核 CPU，进程占 4 核 → 4.0 → 400%）
        metrics.setJvmCpuUsagePercentage(round(jvmCpuLoad * 100, 2));
    }

    // ------------------------------ 内存指标采集 ------------------------------
    private void collectMemoryMetrics(SystemMetrics metrics) {
        // 1. 系统内存指标（总内存、已用内存）
        long systemTotalMem = MEMORY.getTotal(); // 总内存（字节）
        long systemAvailableMem = MEMORY.getAvailable(); // 可用内存（字节）
        long systemUsedMem = systemTotalMem - systemAvailableMem;

        metrics.setSystemMemoryMax(round(systemTotalMem / BYTE_TO_MB, 2));
        metrics.setSystemMemoryUsed(round(systemUsedMem / BYTE_TO_MB, 2));
        metrics.setSystemMemoryUsedPercentage(round((double) systemUsedMem / systemTotalMem * 100, 2));

        // 2. JVM 堆内存指标（默认统计堆内存，非堆可通过 getNonHeapMemoryUsage() 获取）
        MemoryUsage jvmHeapUsage = JVM_MEM_BEAN.getHeapMemoryUsage();
        long jvmHeapUsed = jvmHeapUsage.getUsed();
        long jvmHeapMax = jvmHeapUsage.getMax();

        metrics.setJvmMemoryUsed(round(jvmHeapUsed / BYTE_TO_MB, 2));
        metrics.setJvmMemoryMax(round(jvmHeapMax / BYTE_TO_MB, 2));
        metrics.setJvmMemoryUsedPercentage(round((double) jvmHeapUsed / jvmHeapMax * 100, 2));
    }

    // ------------------------------ 磁盘指标采集 ------------------------------
    private void collectDiskMetrics(SystemMetrics metrics) {
        // 系统磁盘：获取应用所在分区（如 /opt/app 所在的 / 分区）
        File appDir = new File(System.getProperty("user.dir"));
        Optional<OSFileStore> targetStore = FILE_SYSTEM.getFileStores().stream()
                .filter(store -> appDir.getAbsolutePath().startsWith(store.getMount()))
                .findFirst();

        if (targetStore.isPresent()) {
            OSFileStore store = targetStore.get();
            long diskTotal = store.getTotalSpace(); // 总容量（字节）
            long diskUsed = diskTotal - store.getUsableSpace(); // 已用容量（字节）

            metrics.setDiskTotal(round(diskTotal / BYTE_TO_GB, 2));
            metrics.setDiskUsed(round(diskUsed / BYTE_TO_GB, 2));
            metrics.setDiskUsedPercentage(round((double) diskUsed / diskTotal * 100, 2));
        }
    }

    // ------------------------------ Pod 指标采集（K8s 环境） ------------------------------
    private void collectPodMetrics(SystemMetrics metrics) {
        // 1. Pod CPU 使用率（基于 cgroup 配额）
        // Pod CPU 配额：/sys/fs/cgroup/cpu/cpu.cfs_quota_us（默认 -1 表示无限制）
        // Pod CPU 已用：/sys/fs/cgroup/cpu/cpuacct.usage（累计使用时间，纳秒）
        long cpuQuota = readCgroupLongValue("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", -1);
        long cpuPeriod = readCgroupLongValue("/sys/fs/cgroup/cpu/cpu.cfs_period_us", 100000); // 默认 100ms
        long cpuUsedNanos = readCgroupLongValue("/sys/fs/cgroup/cpu/cpuacct.usage", 0);

        double podCpuUsage = 0.0;
        if (cpuQuota > 0) {
            // 计算 1 个周期内的使用率：(已用时间 / 周期) * (配额 / 周期) → 转换为百分比
            double usedRatio = (double) cpuUsedNanos / (cpuPeriod * 1_000_000); // 纳秒 → 周期（微秒）
            podCpuUsage = round(usedRatio * (cpuQuota / (double) cpuPeriod) * 100, 2);
        }
        metrics.setPodCpuUsagePercentage(podCpuUsage);

        // 2. Pod 内存指标（基于 cgroup 配额）
        long podMemLimit = readCgroupLongValue("/sys/fs/cgroup/memory/memory.limit_in_bytes", MEMORY.getTotal());
        long podMemUsed = readCgroupLongValue("/sys/fs/cgroup/memory/memory.usage_in_bytes", 0);

        metrics.setPodMemoryMax(round(podMemLimit / BYTE_TO_MB, 2));
        metrics.setPodMemoryUsed(round(podMemUsed / BYTE_TO_MB, 2));
        metrics.setPodMemoryUsedPercentage(round((double) podMemUsed / podMemLimit * 100, 2));

        // 3. Pod 磁盘指标（获取 PVC 挂载点，如 /data）
        // 假设 PVC 挂载在 /data，可通过配置文件指定挂载点
        String podMountPoint = "/data";
        Optional<OSFileStore> podStore = FILE_SYSTEM.getFileStores().stream()
                .filter(store -> store.getMount().equals(podMountPoint))
                .findFirst();

        if (podStore.isPresent()) {
            OSFileStore store = podStore.get();
            long podDiskTotal = store.getTotalSpace();
            long podDiskUsed = podDiskTotal - store.getUsableSpace();

            metrics.setPodDiskTotal(round(podDiskTotal / BYTE_TO_GB, 2));
            metrics.setPodDiskUsed(round(podDiskUsed / BYTE_TO_GB, 2));
            metrics.setPodDiskUsedPercentage(round((double) podDiskUsed / podDiskTotal * 100, 2));
        }
    }

    // ------------------------------ 辅助方法 ------------------------------
    /**
     * 判断是否为 K8s 环境（检查环境变量或 cgroup 文件）
     */
    private boolean isK8sEnvironment() {
        // 方式1：检查 K8s 环境变量（Pod 内默认存在）
        if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
            return true;
        }
        // 方式2：检查 cgroup 挂载路径（K8s 容器必挂载）
        return new File("/sys/fs/cgroup/cpu/cpu.cfs_quota_us").exists();
    }

    /**
     * 读取 cgroup 文件中的长整型值（Pod 指标专用）
     */
    private long readCgroupLongValue(String path, long defaultValue) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path))).trim();
            return Long.parseLong(content);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 四舍五入保留指定小数位
     */
    private double round(double value, int scale) {
        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }
}