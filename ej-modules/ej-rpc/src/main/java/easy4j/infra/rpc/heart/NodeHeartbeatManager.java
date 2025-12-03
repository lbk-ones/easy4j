package easy4j.infra.rpc.heart;

import cn.hutool.core.net.NetUtil;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.enums.LbType;
import easy4j.infra.rpc.enums.ServerStatus;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.server.RpcServer;
import easy4j.infra.rpc.server.ServerPortChannelManager;
import easy4j.infra.rpc.utils.MetricsCollector;
import easy4j.infra.rpc.utils.SystemMetrics;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

@Slf4j
public class NodeHeartbeatManager {

    static int port;
    private NodeHeartbeatInfo last;
    private long lastRefresh;
    long lastRefreshIntervalTime = 2_000L;
    private int weight;
    protected double maxSystemCpuUsagePercentageThresholds = 90;
    protected double maxJvmCpuUsagePercentageThresholds = 90;
    protected double maxSystemMemoryUsagePercentageThresholds = 90;
    protected double maxDiskUsagePercentageThresholds = 90;

    public NodeHeartbeatManager() {
        this.weight = 1;
    }
    public static void initPort(int port_) {
        port = port_;
    }
    public boolean isOverload(SystemMetrics systemMetrics) {
        if (systemMetrics.getSystemCpuUsagePercentage() > maxSystemCpuUsagePercentageThresholds) {
            log.info(
                    "OverLoad: the system cpu usage: {} is over then the maxSystemCpuUsagePercentageThresholds {}",
                    systemMetrics.getSystemCpuUsagePercentage(), maxSystemCpuUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getJvmCpuUsagePercentage() > maxJvmCpuUsagePercentageThresholds) {
            log.info(
                    "OverLoad: the jvm cpu usage: {} is over then the maxJvmCpuUsagePercentageThresholds {}",
                    systemMetrics.getJvmCpuUsagePercentage(), maxJvmCpuUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getDiskUsedPercentage() > maxDiskUsagePercentageThresholds) {
            log.info("OverLoad: the DiskUsedPercentage: {} is over then the maxDiskUsagePercentageThresholds {}",
                    systemMetrics.getDiskUsedPercentage(), maxDiskUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getSystemMemoryUsedPercentage() > maxSystemMemoryUsagePercentageThresholds) {
            log.info(
                    "OverLoad: the SystemMemoryUsedPercentage: {} is over then the maxSystemMemoryUsagePercentageThresholds {}",
                    systemMetrics.getSystemMemoryUsedPercentage(), maxSystemMemoryUsagePercentageThresholds);
            return true;
        }
        return false;
    }

    public NodeHeartbeatInfo buildHeart() {
        long l = System.currentTimeMillis();
        if (last != null && lastRefresh > 0 && (l - lastRefresh < lastRefreshIntervalTime)) {
            return last;
        }
        SystemMetrics systemMetrics = new MetricsCollector().collectAllMetrics();

        E4jRpcConfig config = IntegratedFactory.getRpcConfig().getConfig();
        weight = Math.max(config.getServer().getWeight(),1);
        int processID = getProcessID();
        last = new NodeHeartbeatInfo()
                .setProcessId(processID)
                .setStartupTime(RpcServer.getStartTime())
                .setReportTime(l)
                .setJvmCpuUsage(systemMetrics.getJvmCpuUsagePercentage())
                .setCpuUsage(systemMetrics.getSystemCpuUsagePercentage())
                .setJvmMemoryUsage(systemMetrics.getJvmMemoryUsedPercentage())
                .setMemoryUsage(systemMetrics.getSystemMemoryUsedPercentage())
                .setDiskUsage(systemMetrics.getDiskUsedPercentage())
                .setServerStatus(isOverload(systemMetrics) ? ServerStatus.BUSY : ServerStatus.NORMAL)
                .setHost(NetUtil.getLocalhost().toString())
                .setWeight(weight)
                .setConn(ServerPortChannelManager.countChannelByPort(port))
                .setPort(port);

        // dynamic weight
        dynamicWeight(config, systemMetrics);

        lastRefresh = l;
        return last;
    }

    private void dynamicWeight(E4jRpcConfig config, SystemMetrics systemMetrics) {
        LbType lbType = config.getLbType();
        if(lbType == LbType.PERFORMANCE_BASED){
            int baseWeight = weight * 2;
            int dynamicWeight;
            if (systemMetrics.getSystemCpuUsagePercentage() > 90) {
                dynamicWeight = baseWeight; // CPU 过载，权重降到最低
            } else if (systemMetrics.getSystemCpuUsagePercentage() > 70) {
                dynamicWeight = baseWeight / 2; // 高负载，权重减半
            } else if (systemMetrics.getSystemCpuUsagePercentage() < 30) {
                dynamicWeight = baseWeight * 2; // 低负载，权重翻倍
            } else {
                dynamicWeight = baseWeight; // 正常负载，用基础权重
            }
            last.setWeight(dynamicWeight);
        }
    }


    public static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.parseInt(runtimeMXBean.getName().split("@")[0]);
    }

}
