package easy4j.module.idempotent;

import easy4j.module.base.plugin.idempotent.Easy4jIdempotentKeyGenerator;
import easy4j.module.base.plugin.idempotent.Easy4jIdempotentStorage;
import easy4j.module.idempotent.rules.*;
import easy4j.module.idempotent.rules.datajdbc.Easy4jIdempotentDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import javax.sql.DataSource;
@Configuration
@ComponentScan(value={"easy4j.module.idempotent.rules.datajdbc"})
@EnableJdbcRepositories(basePackageClasses = {Easy4jIdempotentDao.class})
public class Config {
    @Bean(name = "easy4jIdempotentWebConfig")
    public WebConfig webConfig(){
        return new WebConfig();
    }

    @Bean("idempotentHandlerInterceptor")
    public IdempotentHandlerInterceptor idempotentHandlerInterceptor(){
        return new IdempotentHandlerInterceptor();
    }

    @Bean
    public IdempotentToolFactory idempotentToolFactory(){
        return new IdempotentToolFactory();
    }




    @Bean("headerKeyGenerator")
    public Easy4jIdempotentKeyGenerator takeKeyGenerator(){
        return new HeaderEasy4jIdempotentKeyGenerator();
    }

    @Bean("queryKeyGenerator")
    public Easy4jIdempotentKeyGenerator queryKeyGenerator(){
        return new QueryEasy4jIdempotentKeyGenerator();
    }

    @Bean("formKeyGenerator")
    public Easy4jIdempotentKeyGenerator formKeyGenerator(){
        return new FormEasy4jIdempotentKeyGenerator();
    }

    @Bean("dbIdempotentStorage")
    @ConditionalOnBean(DataSource.class)
    public Easy4jIdempotentStorage dbIdempotentStorage(){
        return new DbEasy4jIdempotentStorage();
    }


}
