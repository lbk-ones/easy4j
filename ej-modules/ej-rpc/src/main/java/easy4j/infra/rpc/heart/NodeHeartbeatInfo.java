package easy4j.infra.rpc.heart;

import easy4j.infra.rpc.enums.ServerStatus;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 节点心跳信息
 *
 * @since 2.0.1
 */
@Data
@Accessors(chain = true)
public class NodeHeartbeatInfo {
    /**
     * 进程ID
     */
    protected int processId;
    /**
     * 启动时间
     */
    protected long startupTime;
    /**
     * 上报时间
     */
    protected long reportTime;
    /**
     * jvm cpu利用率
     */
    protected double jvmCpuUsage;
    /**
     * 机器cpu使用率
     */
    protected double cpuUsage;
    /**
     * jvm 内存使用率
     */
    protected double jvmMemoryUsage;
    /**
     * 机器内存使用率
     */
    protected double memoryUsage;
    /**
     * 磁盘使用率
     */
    protected double diskUsage;
    /**
     * 机器状态，正常\繁忙
     */
    protected ServerStatus serverStatus;

    /**
     * 主机host
     */
    protected String host;

    /**
     * 端口
     */
    protected int port;

    /**
     * 加权
     */
    protected int weight = 1;


    /**
     * 连接数
     */
    protected int conn;

    /**
     * 是否禁用
     */
    private boolean disabled = false;

}
