package easy4j.infra.rpc.integrated.spring;

import easy4j.infra.rpc.config.E4jRpcConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "easy4j.rpc")
public class E4jRpcConfigSpring extends E4jRpcConfig {
}
