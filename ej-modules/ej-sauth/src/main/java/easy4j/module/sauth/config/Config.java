package easy4j.module.sauth.config;


import easy4j.module.base.module.Module;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import easy4j.module.sauth.authentication.DefaultSecurityAuthentication;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.AuthorizationStrategy;
import easy4j.module.sauth.authorization.DefaultAuthorizationStrategy;
import easy4j.module.sauth.context.Easy4jSecurityContext;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.DefaultEncryptionService;
import easy4j.module.sauth.core.Easy4jSecurityService;
import easy4j.module.sauth.core.EncryptionService;
import easy4j.module.sauth.core.SecurityService;
import easy4j.module.sauth.filter.Easy4jSecurityFilterInterceptor;
import easy4j.module.sauth.filter.FilterConfig;
import easy4j.module.sauth.session.DbSessionStrategy;
import easy4j.module.sauth.session.SessionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class Config implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {

        log.info(SysLog.compact("初始化sauth模块成功"));

    }

    @Bean
    @ConditionalOnMissingBean(SecurityContext.class)
    public SecurityContext securityContext() {
        return new Easy4jSecurityContext();
    }

    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(SessionStrategy.class)
    public SessionStrategy sessionStrategy() {
        return new DbSessionStrategy();
    }

    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(AuthorizationStrategy.class)
    public AuthorizationStrategy authorizationStrategy() {
        return new DefaultAuthorizationStrategy();
    }

    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnBean(DataSource.class)
    public SecurityService securityService() {
        return new Easy4jSecurityService(
                securityAuthentication(),
                sessionStrategy(),
                authorizationStrategy()
        );
    }

    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnBean(DataSource.class)
    public FilterConfig filterConfig() {
        return new FilterConfig(easy4jSecurityFilterInterceptor());
    }

    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnBean(value = {DataSource.class})
    public Easy4jSecurityFilterInterceptor easy4jSecurityFilterInterceptor() {
        return new Easy4jSecurityFilterInterceptor(
                sessionStrategy(),
                securityContext(),
                authorizationStrategy(),
                securityAuthentication()
        );
    }

    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(SecurityAuthentication.class)
    public SecurityAuthentication securityAuthentication() {
        return new DefaultSecurityAuthentication(
                authorizationStrategy(),
                encryptionService(),
                sessionStrategy(),
                securityContext()
        );
    }

    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(EncryptionService.class)
    public EncryptionService encryptionService() {
        return new DefaultEncryptionService();
    }
}
