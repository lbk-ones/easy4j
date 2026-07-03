package io.github.lbkones.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Easy4jSentinelAutoConfiguration {


    @Bean
    @ConditionalOnProperty(value = "spring.cloud.sentinel.filter.enabled",havingValue = "true")
    public BlockExceptionHandler globalBlockExceptionHandler(){
        return new DefaultWebGlobalSentinelExceptionHandler();
    }

}
