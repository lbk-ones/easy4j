package io.github.lbkones.config.nacos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "spring.cloud.nacos.config.enabled", havingValue = "true", matchIfMissing = true)
public class ScaNacosAutoConfiguration {


    @Bean
    public ScaRunner scaRunner() {
        return new ScaRunner();
    }

}
