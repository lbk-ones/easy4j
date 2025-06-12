package easy4j.infra.actuator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public CacheEndpoint cacheEndpoint() {
        return new CacheEndpoint();
    }
}
