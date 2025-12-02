package easy4j.infra.rpc.config;

import easy4j.infra.rpc.enums.LbType;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.enums.RegisterType;
import easy4j.infra.rpc.enums.SerializableType;
import lombok.Data;

/**
 * netty的一些基础配置
 *
 * @since 2.0.1
 */
@Data
public class BaseConfig {

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
     * 是否允许端口复用（解决端口占用 “TIME_WAIT” 状态导致的启动失败）
     */
    private boolean soReuseAddr = true;

    /**
     * 服务端半连接大小
     */
    private Integer soBackLog = 2048;

    /**
     * Socket 读操作超时,已连接后的数据读写操作
     * 控制阻塞 I/O 模式下，Socket 读操作的最大等待时间（避免读操作无限期阻塞）
     * NIO下无效
     */
    private Integer soTimeOut = 2048;

    /**
     * 客户端TCP三次握手阶段连接超时，控制客户端与服务器简历连接的最大耗时，避免客户端无限期等待连接建立，默认3000ms
     */
    private Integer connectTimeOutMillis = 3000;

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
}
