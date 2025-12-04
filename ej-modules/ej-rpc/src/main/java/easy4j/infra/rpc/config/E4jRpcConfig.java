package easy4j.infra.rpc.config;

import easy4j.infra.rpc.enums.LbType;
import easy4j.infra.rpc.enums.RegisterType;
import easy4j.infra.rpc.enums.SerializableType;
import lombok.Data;

@Data
public class E4jRpcConfig {

    public E4jRpcConfig() {
        this.server = new E4jServerConfig();
        this.client = new E4jClientConfig();
    }

    /**
     * 序列化方式 默认jackson
     */
    private SerializableType serializableType = SerializableType.JACKSON;

    /**
     * 注册中心类型 默认为jdbc
     */
    private RegisterType registerType = RegisterType.JDBC;

    /**
     * 负载均衡的方式
     */
    private LbType lbType = LbType.ROUND_ROBIN;

    /**
     * 禁用Nagle算法，确保小数据包即使发送，降低延迟
     */
    private boolean tcpNodeDelay = false;

    /**
     * 启用tcp心跳保活
     */
    private boolean soKeepLive = true;

    /**
     * Socket 读操作超时,已连接后的数据读写操作
     * 控制阻塞 I/O 模式下，Socket 读操作的最大等待时间（避免读操作无限期阻塞）
     * NIO下无效(默认配置它不生效)
     */
    @Deprecated
    private Integer soTimeOut = 2048;


    /**
     * 设置 Socket 发送缓冲区大小（底层操作系统缓冲区）
     * 默认系统自动优化
     */
    private Integer soSndBuf = null;

    /**
     * 设置 Socket 接收缓冲区大小（底层操作系统缓冲区）
     * 默认系统自动优化
     */
    private Integer soRcvBuf = null;

    /**
     * 写缓冲区高水位线（超过则触发 ChannelWritabilityChanged 事件）
     * 控制写操作背压，默认值约 64KB，需配合 isWritable() 判断避免内存溢出
     */
    private Integer writeBufferHighWaterMark = 128 * 1024;

    /**
     * 恢复 “可写” 状态的阈值：缓冲区字节数 < 该值时，通道恢复可写
     */
    private Integer writeBufferLowWaterMark = 64 * 1024;


    /**
     * 注册中心 jdbc url
     */
    private String registryJdbcUrl;

    /**
     * 注册中心jdbc 用户名
     */
    private String registryJdbcUsername;

    /**
     * 注册中心jdbc 密码
     */
    private String registryJdbcPassword;

    /**
     * 注册中心jdbc 扫描间隔时间 单位 milliseconds 默认10秒
     */
    private long registryJdbcCheckPeriod = 1000 * 10L;


    /**
     * 服务端配置
     */
    private E4jServerConfig server;

    /**
     * 客户端配置
     */
    private E4jClientConfig client;

}
