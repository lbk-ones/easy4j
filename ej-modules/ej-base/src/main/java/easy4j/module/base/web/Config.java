package easy4j.module.base.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public PerRequestInterceptor perRequestLifecycleInterceptor() {
        return new PerRequestInterceptor();
    }
}
