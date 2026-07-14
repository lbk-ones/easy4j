package io.github.lbkones.nacos.pure;

import org.springframework.context.annotation.Bean;

/**
 * 整合spring alibaba nacos config 和 spring cloud nacos config
 */
public class NacosCustomAutoConfiguration {


    @Bean
    public CloudPropertiesRefresh propertiesRefresh(){
        return new CloudPropertiesRefresh();
    }

    @Bean
    public CloudPropertiesRefreshHolder propertiesRefreshHolder(){
        return new CloudPropertiesRefreshHolder();
    }

}
