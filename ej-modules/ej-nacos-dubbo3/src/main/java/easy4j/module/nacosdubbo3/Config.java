package easy4j.module.nacosdubbo3;


import easy4j.module.nacosdubbo3.hot.PostBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = ConfigProperties.class)
public class Config {


    @Bean
    public PostBean postBean(){

        return new PostBean();
    }


}
