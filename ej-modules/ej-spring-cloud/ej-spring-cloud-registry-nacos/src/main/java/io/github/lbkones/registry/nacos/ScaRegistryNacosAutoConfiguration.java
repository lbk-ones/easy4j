package io.github.lbkones.registry.nacos;

import io.github.lbkones.cloud.openfeign.ScaOpenFeignAutoConfiguration;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@AutoConfigureAfter(value = ScaOpenFeignAutoConfiguration.class)
public class ScaRegistryNacosAutoConfiguration {

    @Resource
    RestTemplate restTemplate;

    @Bean
    public NamingServerInvoker namingServerInvoker() {
        return NamingServerInvoker.createByEnv(restTemplate);
    }

    @Bean
    public NacosEventListener nacosEventListener() {
        return new NacosEventListener();
    }

}
