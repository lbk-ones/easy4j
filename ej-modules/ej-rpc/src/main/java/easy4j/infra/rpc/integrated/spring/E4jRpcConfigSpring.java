package easy4j.infra.rpc.integrated.spring;

import easy4j.infra.rpc.config.E4jRpcConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "easy4j.rpc")
public class E4jRpcConfigSpring extends E4jRpcConfig {

}
