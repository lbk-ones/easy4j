package easy4j.infra.rpc.config;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 客户端配置
 *
 * @author bokun.li
 * @since 2.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ClientConfig extends BaseConfig {

    /**
     * 要连接的主机
     */
    private String host;

    /**
     * 要连接的端口
     */
    private Integer port;

    /**
     * 调用超时时间
     * 调用开始 等待超过这个时间未返回那么就报错 默认30秒
     */
    private Integer invokeTimeOutMillis = 1000 * 30;




}
