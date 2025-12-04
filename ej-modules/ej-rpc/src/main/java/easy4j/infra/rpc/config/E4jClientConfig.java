package easy4j.infra.rpc.config;

import lombok.Data;

@Data
public class E4jClientConfig {

    /**
     * 检查服务提供者是否存在
     */
    private boolean check = true;

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

    /**
     * 客户端TCP三次握手阶段连接超时，控制客户端与服务器简历连接的最大耗时，避免客户端无限期等待连接建立，默认3000ms
     */
    private Integer connectTimeOutMillis = 3000;


    /**
     * 最大重连次数
     */
    private int reconnectMaxRetryCount = 5;

}