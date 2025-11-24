package easy4j.infra.rpc.config;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 服务端配置
 *
 * @author bokun.li
 * @since 2.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ServerConfig extends BaseConfig {

    /**
     * 要监听的端口
     */
    private Integer port;

}
