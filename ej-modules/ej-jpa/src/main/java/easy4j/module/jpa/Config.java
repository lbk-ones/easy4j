package easy4j.module.jpa;

import easy4j.module.jpa.aware.EasyJpaAuditorAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
    @ConditionalOnBean(value = EasyJpaAuditorAware.class)
    public JpaAuditorAware jpaAuditorAware(){
        return new JpaAuditorAware();
    }
    @Bean
    public HibernateConfig hibernateConfig(){
        return new HibernateConfig();
    }
}