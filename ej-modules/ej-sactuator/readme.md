以下是 Micrometer 默认指标的中文解释，按类别分组并标注核心用途：

### 一、应用生命周期指标

| 指标名称                       | 中文释义     | 用途说明                                    |
|----------------------------|----------|-----------------------------------------|
| `application.ready.time`   | 应用就绪时间   | 应用完成初始化并准备好接收请求的时间戳（毫秒），用于监控启动阶段耗时。     |
| `application.started.time` | 应用启动完成时间 | 应用完全启动并可服务的时间戳（毫秒），与 ready 对比可分析预热阶段耗时。 |

### 二、缓存指标（以 Redis/Caffeine 等为例）

| 指标名称                    | 中文释义   | 用途说明                                         |
|-------------------------|--------|----------------------------------------------|
| `cache.eviction.weight` | 缓存淘汰权重 | 被淘汰缓存条目的总权重（若缓存支持权重策略），反映淘汰策略的执行代价。          |
| `cache.evictions`       | 缓存淘汰次数 | 因空间不足或过期被自动删除的缓存条目数，高值可能表示缓存配置过小。            |
| `cache.gets`            | 缓存读取次数 | 按结果分为命中（hit）和未命中（miss），计算缓存命中率（hit/miss 比例）。 |
| `cache.puts`            | 缓存写入次数 | 新写入或更新缓存条目的次数，监控缓存更新频率。                      |
| `cache.size`            | 缓存当前大小 | 当前缓存中存储的条目数量，用于监控缓存增长趋势和空间使用情况。              |

### 三、磁盘指标

| 指标名称         | 中文释义   | 用途说明                              |
|--------------|--------|-----------------------------------|
| `disk.free`  | 磁盘剩余空间 | 可用磁盘空间字节数，低于阈值时触发告警（如剩余空间 < 1GB）。 |
| `disk.total` | 磁盘总空间  | 磁盘总容量字节数，结合 free 计算使用率。           |

### 四、线程池指标（如 `@Async` 或自定义 Executor）

| 指标名称                       | 中文释义      | 用途说明                                |
|----------------------------|-----------|-------------------------------------|
| `executor.active`          | 活跃线程数     | 当前正在执行任务的线程数量，持续高值可能表示线程池配置过小。      |
| `executor.completed`       | 已完成任务数    | 线程池已处理的任务总数，用于统计吞吐量。                |
| `executor.pool.core`       | 核心线程数     | 线程池的基本线程数量（初始化时创建）。                 |
| `executor.pool.max`        | 最大线程数     | 线程池允许的最大线程数量，达到此值后新任务会进入队列。         |
| `executor.pool.size`       | 当前线程数     | 线程池当前实际拥有的线程数量（包括空闲和活跃线程）。          |
| `executor.queue.remaining` | 队列剩余容量    | 任务队列中剩余的空闲位置，接近 0 时表示队列将满，可能导致任务拒绝。 |
| `executor.queued`          | 队列中等待的任务数 | 当前在队列中等待执行的任务数量，高值可能表示处理能力不足。       |

### 五、HTTP 请求指标（最核心的性能指标）

| 指标名称                   | 中文释义      | 用途说明                                                |
|------------------------|-----------|-----------------------------------------------------|
| `http.server.requests` | HTTP 请求指标 | 包含请求计数、响应时间分布（如 P50/P95）、按 URI 和状态码分组统计，是接口性能的核心指标。 |

### 六、JVM 指标（重点关注）

#### 1. 内存指标

| 指标名称                        | 中文释义       | 用途说明                                |
|-----------------------------|------------|-------------------------------------|
| `jvm.buffer.count`          | 缓冲区数量      | JVM 中 ByteBuffer 的数量，用于监控直接内存使用情况。  |
| `jvm.buffer.memory.used`    | 缓冲区已用内存    | 所有 ByteBuffer 当前使用的内存总量（字节）。        |
| `jvm.buffer.total.capacity` | 缓冲区总容量     | 所有 ByteBuffer 的总容量（字节），反映直接内存预分配情况。 |
| `jvm.memory.committed`      | 已提交内存      | JVM 已向操作系统申请的内存（字节），即使未完全使用也会被保留。   |
| `jvm.memory.max`            | 最大可用内存     | JVM 内存区域的最大容量（如堆的 -Xmx 参数值）。        |
| `jvm.memory.usage.after.gc` | GC 后的内存使用率 | 垃圾回收后各内存区域的使用比例，用于分析内存碎片和对象存活情况。    |
| `jvm.memory.used`           | 当前已用内存     | 各内存区域（堆/非堆）的当前使用量（字节），监控内存泄漏的核心指标。  |

#### 2. 类加载指标

| 指标名称                   | 中文释义   | 用途说明                                  |
|------------------------|--------|---------------------------------------|
| `jvm.classes.loaded`   | 已加载类数量 | 当前 JVM 已加载的类总数，异常增长可能表示类加载器泄漏（如频繁部署）。 |
| `jvm.classes.unloaded` | 已卸载类数量 | JVM 卸载的类数量，通常值很小，大量卸载可能表示内存压力大。       |

#### 3. GC 指标

| 指标名称                      | 中文释义      | 用途说明                                           |
|---------------------------|-----------|------------------------------------------------|
| `jvm.gc.live.data.size`   | 存活数据大小    | GC 后存活对象占用的内存大小（字节），反映应用的内存基线。                 |
| `jvm.gc.max.data.size`    | 最大数据大小    | 堆内存中可容纳的最大数据量（字节），通常等于堆的最大值。                   |
| `jvm.gc.memory.allocated` | 内存分配量     | 新生代中已分配的内存总量（字节），增长过快可能导致频繁 GC。                |
| `jvm.gc.memory.promoted`  | 晋升到老年代的内存 | 从新生代晋升到老年代的对象占用的内存总量（字节），高值可能导致老年代 GC 频繁。      |
| `jvm.gc.overhead`         | GC 开销比例   | GC 操作占用的 CPU 时间比例，过高（如 > 10%）会影响应用性能。          |
| `jvm.gc.pause`            | GC 暂停时间   | GC 导致应用暂停的时间分布（如 P99），过长的暂停会影响响应时间（如 > 500ms）。 |

#### 4. 线程指标

| 指标名称                 | 中文释义   | 用途说明                                                |
|----------------------|--------|-----------------------------------------------------|
| `jvm.threads.daemon` | 守护线程数  | 当前 JVM 中的守护线程数量（如 GC 线程、监控线程）。                      |
| `jvm.threads.live`   | 活跃线程数  | 当前 JVM 中的总线程数量，包括守护线程和用户线程。                         |
| `jvm.threads.peak`   | 线程峰值数量 | 自 JVM 启动以来，线程数量的最大值，用于分析线程增长趋势。                     |
| `jvm.threads.states` | 线程状态分布 | 按状态（RUNNABLE、WAITING、BLOCKED 等）统计的线程数量，用于定位死锁或阻塞问题。 |

### 七、日志指标（Logback）

| 指标名称             | 中文释义   | 用途说明                                        |
|------------------|--------|---------------------------------------------|
| `logback.events` | 日志事件数量 | 按级别（ERROR、WARN、INFO 等）统计的日志输出次数，用于监控异常日志频率。 |

### 八、系统资源指标

| 指标名称                 | 中文释义       | 用途说明                                                   |
|----------------------|------------|--------------------------------------------------------|
| `process.cpu.usage`  | 进程 CPU 使用率 | 应用进程占用的 CPU 比例（0-1 之间），持续高值（如 > 0.8）可能表示存在性能瓶颈。        |
| `process.start.time` | 进程启动时间     | 应用启动的时间戳（毫秒），用于计算运行时长。                                 |
| `process.uptime`     | 进程运行时间     | 应用已连续运行的时间（秒），异常重启可能表示存在稳定性问题。                         |
| `system.cpu.count`   | 系统 CPU 核心数 | 服务器的 CPU 物理核心数量，用于评估系统资源上限。                            |
| `system.cpu.usage`   | 系统 CPU 使用率 | 整个系统的 CPU 使用率（0-1 之间），结合 process.cpu.usage 判断应用对系统的压力。 |

### 九、Tomcat 会话指标（如使用 Tomcat 作为容器）

| 指标名称                             | 中文释义     | 用途说明                              |
|----------------------------------|----------|-----------------------------------|
| `tomcat.sessions.active.current` | 当前活跃会话数  | Tomcat 中当前活跃的 HTTP 会话数量，反映用户并发情况。 |
| `tomcat.sessions.active.max`     | 最大活跃会话数  | 历史上同时活跃的会话最大数量，用于评估峰值并发压力。        |
| `tomcat.sessions.alive.max`      | 会话最大存活时间 | 单个会话的最长存活时间（秒），用于分析会话保持策略是否合理。    |
| `tomcat.sessions.created`        | 已创建会话数   | Tomcat 自启动以来创建的会话总数，结合时间计算会话创建速率。 |
| `tomcat.sessions.expired`        | 已过期会话数   | 因超时自动过期的会话数量，高值可能表示会话超时设置过短。      |
| `tomcat.sessions.rejected`       | 被拒绝的会话数  | 因达到最大会话限制而被拒绝的会话数量，非零值表示需要增加会话上限。 |

### 关键指标优先级建议

1. **性能类**：`http.server.requests`（响应时间和请求数）、`jvm.gc.pause`（GC 暂停时间）
2. **资源类**：`jvm.memory.used`（内存使用）、`process.cpu.usage`（CPU 使用率）、`disk.free`（磁盘空间）
3. **稳定性类**：`jvm.threads.states`（线程状态）、`logback.events{level="error"}`（错误日志频率）
4. **容量规划类**：`http.server.requests.count`（请求量趋势）、`tomcat.sessions.active.current`（会话并发）

通过监控这些指标，可快速定位性能瓶颈、资源耗尽风险和稳定性问题，是构建系统可观测性的基础。

```json
{
  "names": [
    "application.ready.time",
    "application.started.time",
    "cache.eviction.weight",
    "cache.evictions",
    "cache.gets",
    "cache.puts",
    "cache.size",
    "disk.free",
    "disk.total",
    "executor.active",
    "executor.completed",
    "executor.pool.core",
    "executor.pool.max",
    "executor.pool.size",
    "executor.queue.remaining",
    "executor.queued",
    "http.server.requests",
    "jvm.buffer.count",
    "jvm.buffer.memory.used",
    "jvm.buffer.total.capacity",
    "jvm.classes.loaded",
    "jvm.classes.unloaded",
    "jvm.gc.live.data.size",
    "jvm.gc.max.data.size",
    "jvm.gc.memory.allocated",
    "jvm.gc.memory.promoted",
    "jvm.gc.overhead",
    "jvm.gc.pause",
    "jvm.memory.committed",
    "jvm.memory.max",
    "jvm.memory.usage.after.gc",
    "jvm.memory.used",
    "jvm.threads.daemon",
    "jvm.threads.live",
    "jvm.threads.peak",
    "jvm.threads.states",
    "logback.events",
    "process.cpu.usage",
    "process.start.time",
    "process.uptime",
    "system.cpu.count",
    "system.cpu.usage",
    "tomcat.sessions.active.current",
    "tomcat.sessions.active.max",
    "tomcat.sessions.alive.max",
    "tomcat.sessions.created",
    "tomcat.sessions.expired",
    "tomcat.sessions.rejected"
  ]
}
```