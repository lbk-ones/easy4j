package easy4j.infra.rpc.integrated.spring;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.config.E4jClientConfig;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.config.E4jServerConfig;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "easy4j.rpc")
public class E4jRpcConfigSpring extends E4jRpcConfig {

}
