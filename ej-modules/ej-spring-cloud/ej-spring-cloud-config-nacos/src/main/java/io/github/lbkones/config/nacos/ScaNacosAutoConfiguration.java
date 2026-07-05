package io.github.lbkones.config.nacos;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScaNacosAutoConfiguration {


    @Bean
    public ScaRunner scaRunner(){
        return new ScaRunner();
    }

}
