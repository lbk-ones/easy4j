package easy4j.module.sauth.config;


import easy4j.module.base.module.Module;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.sauth.core.DefaultSecurityAuthentication;
import easy4j.module.sauth.core.Easy4jSecurityService;
import easy4j.module.sauth.core.SecurityAuthentication;
import easy4j.module.sauth.core.SecurityService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class Config {


    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnBean(DataSource.class)
    public SecurityService securityService() {
        return new Easy4jSecurityService();
    }

    /**
     * 由服务来重写默认的认证逻辑
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(SecurityAuthentication.class)
    public SecurityAuthentication securityAuthentication() {
        return new DefaultSecurityAuthentication();
    }
}
