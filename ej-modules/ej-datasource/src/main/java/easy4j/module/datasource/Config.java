package easy4j.module.datasource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@EnableConfigurationProperties({DataSourceProperties.class})
@Configuration
public class Config {

    @Bean
    @ConfigurationProperties("spring.datasource.druid.filter.log4j3")
    @ConditionalOnProperty(
            prefix = "spring.datasource.druid.filter.log4j3",
            name = {"enabled"}
    )
    @ConditionalOnMissingBean
    public Log4j3Filter log4j3Filter() {
        return new Log4j3Filter();
    }

}