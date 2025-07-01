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


import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.module.ModuleBoolean;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.EventPublisher;
import easy4j.infra.context.event.NacosSauthServerRegisterEvent;
import easy4j.infra.dbaccess.DBAccessFactory;
import easy4j.module.sauth.authentication.DefaultSecurityAuthentication;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.DefaultAuthorizationStrategy;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.Easy4jSecurityContext;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.controller.SAuthController;
import easy4j.module.sauth.core.*;
import easy4j.module.sauth.enums.SecuritySessionType;
import easy4j.module.sauth.session.DbSessionStrategy;
import easy4j.module.sauth.session.RedisSessionStrategy;
import easy4j.module.sauth.session.SessionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
@Configuration
public class Config implements CommandLineRunner {

    public static final String AUTH_SERVER_NAME = "easy4j-sauth-server";

    @Resource
    EventPublisher eventPublisher;

    @Override
    public void run(String... args) throws Exception {
        boolean property = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_SERVER, boolean.class);
        if (property && isServer) {

            eventPublisher.publishEvent(new NacosSauthServerRegisterEvent(this, AUTH_SERVER_NAME));

            log.info(SysLog.compact("sauth module begin init..."));
            DBAccessFactory.initDb("db/auth");

        }
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
    @ModuleBoolean(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(SecurityAuthorization.class)
    public SecurityAuthorization authorizationStrategy() {
        return new DefaultAuthorizationStrategy();
    }

    // 核心业务类
    @Bean
    @ModuleBoolean(SysConstant.EASY4J_SAUTH_ENABLE)
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
//    @Bean
//    @Module(SysConstant.EASY4J_SAUTH_ENABLE)
//    @ConditionalOnBean(value = {DataSource.class})
//    public Easy4jSecurityFilterInterceptor easy4jSecurityFilterInterceptor() {
//        return new Easy4jSecurityFilterInterceptor(
//                sessionStrategy(),
//                securityContext(),
//                authorizationStrategy(),
//                securityAuthentication()
//        );
//    }

    // 权限认证
    @Bean
    @ModuleBoolean(SysConstant.EASY4J_SAUTH_ENABLE)
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
    @ModuleBoolean(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(EncryptionService.class)
    public EncryptionService encryptionService() {
        return new DefaultEncryptionService();
    }


    // 权限认证
    @Bean
    @ModuleBoolean(SysConstant.EASY4J_SAUTH_ENABLE)
    @ConditionalOnMissingBean(LoadUserByUserName.class)
    public LoadUserByUserName loadUserByUserName() {
        return new DefaultLoadUserByUserName();
    }

    @Bean
    @ModuleBoolean(SysConstant.EASY4J_SAUTH_ENABLE)
    public SAuthController sAuthController() {
        return new SAuthController();
    }
}
