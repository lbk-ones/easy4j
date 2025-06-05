/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.sauth.config;


import easy4j.module.base.module.Module;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import easy4j.module.sauth.authentication.DefaultSecurityAuthentication;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.DefaultAuthorizationStrategy;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.Easy4jSecurityContext;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.DefaultEncryptionService;
import easy4j.module.sauth.core.Easy4jSecurityService;
import easy4j.module.sauth.core.EncryptionService;
import easy4j.module.sauth.core.SecurityService;
import easy4j.module.sauth.enums.SecuritySessionType;
import easy4j.module.sauth.filter.Easy4jSecurityFilterInterceptor;
import easy4j.module.sauth.session.DbSessionStrategy;
import easy4j.module.sauth.session.RedisSessionStrategy;
import easy4j.module.sauth.session.SessionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
@Configuration
public class Config implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {

        log.info(SysLog.compact("sauth module inited "));

    }

    // 上下文
    @Bean
    @ConditionalOnMissingBean(SecurityContext.class)
    public SecurityContext securityContext() {
        return new Easy4jSecurityContext();
    }

    // 会话策略
    @Bean
    @ConditionalOnMissingBean(SessionStrategy.class)
    public SessionStrategy sessionStrategy() {
        String property = Easy4j.getProperty(SysConstant.EASY4J_AUTH_SESSION_STORAGE_TYPE);
        if (SecuritySessionType.DB.name().equalsIgnoreCase(property)) {
            return new DbSessionStrategy();
        } else {
            return new RedisSessionStrategy();
        }
    }

    // 授权机制
    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(SecurityAuthorization.class)
    public SecurityAuthorization authorizationStrategy() {
        return new DefaultAuthorizationStrategy();
    }

    // 核心业务类
    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnBean(DataSource.class)
    public SecurityService securityService() {
        return new Easy4jSecurityService(
                securityAuthentication(),
                sessionStrategy(),
                authorizationStrategy(),
                securityContext()
        );
    }

//    @Bean
//    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
//    @ConditionalOnBean(DataSource.class)
//    public FilterConfig filterConfig() {
//        return new FilterConfig(easy4jSecurityFilterInterceptor());
//    }

    // 拦截器
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

    // 权限认证
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

    //密码加密方式
    @Bean
    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(EncryptionService.class)
    public EncryptionService encryptionService() {
        return new DefaultEncryptionService();
    }
}
