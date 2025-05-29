package easy4j.module.base.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@Configuration
public class Config {

    @Bean
    public PerRequestInterceptor perRequestLifecycleInterceptor() {
        return new PerRequestInterceptor();
    }
}