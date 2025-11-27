package easy4j.infra.rpc.config;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 服务端配置
 *
 * @author bokun.li
 * @since 2.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ServerConfig extends BaseConfig {


    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 要监听的端口
     */
    private Integer port;

}
