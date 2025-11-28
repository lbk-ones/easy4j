package easy4j.infra.rpc.config;

import easy4j.infra.rpc.integrated.SpringServerInstanceInit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring整合配置
 *
 * @author bokun
 * @since 2.0.1
 */
@Configuration
public class SpringConfig {

    @Bean
    public SpringServerInstanceInit springServerInstanceInit() {
        return new SpringServerInstanceInit();
    }

}
