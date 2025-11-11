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
package easy4j.module.sauth.session;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.sca.Easy4jNacosInvokerApi;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.DBAccessFactory;
import easy4j.module.sauth.config.Config;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.SecuritySession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import javax.sql.DataSource;

import java.util.concurrent.TimeUnit;

/**
 * DbSessionStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
@ConditionalOnBean(DataSource.class)
public class DbSessionStrategy extends AbstractSessionStrategy implements InitializingBean {
    public static final String GET_SESSION = "/sauth/getSession";
    public static final String SAVE_SESSION = "/sauth/saveSession";
    public static final String DELETE_SESSION = "/sauth/deleteSession";
    public static final String REFRESH_SESSION = "/sauth/refreshSession";
    public static final String GET_SESSION_BY_USER_NAME = "/sauth/getSessionByUserName";

    private static DBAccess dbAccess;


    Easy4jNacosInvokerApi easy4jNacosInvokerApi;

    @Resource
    Easy4jContext easy4jContext;

    @Resource
    SecurityContext securityContext;

    boolean isClient;

    String serverName;

    @Override
    public void afterPropertiesSet() throws Exception {
        dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class), true, true);
        easy4jNacosInvokerApi = easy4jContext.get(Easy4jNacosInvokerApi.class);

        boolean property = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_SERVER, boolean.class);
        if (!isServer && property) {
            serverName = Config.AUTH_SERVER_NAME;
            isClient = true;
        }
        // server run session clear thread
        if (isServer && property) {
            scheduleClear();
        }
    }

    private void scheduleClear() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.MINUTES.sleep(5L);
                    clearInValidSession();
                } catch (InterruptedException e) {
                    log.error("db-session-clear-thread 被中断，准备退出");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("user db session clear occurred exception", e);
                }
            }
            log.info("db-session-clear-thread 已退出");
        });
        thread.setDaemon(true);
        thread.setName("db-session-clear-thread");
        thread.start();
    }

    @Override
    public SecuritySession getSession(String token) {
        if (isClient) {
            // cache
            ISecurityEasy4jSession session = securityContext.getSessionByToken(token);
            if (session == null) {
                NacosInvokeDto build = NacosInvokeDto.builder()
                        .group(SysConstant.NACOS_AUTH_GROUP)
                        .serverName(serverName)
                        .accessToken(token)
                        .path(GET_SESSION + SP.SLASH + token)
                        .build();

                EasyResult<Object> securitySessionEasyResult = easy4jNacosInvokerApi.get(build);
                CheckUtils.checkRpcRes(securitySessionEasyResult);
                session = CheckUtils.convertRpcRes(securitySessionEasyResult, SecuritySession.class);
                securityContext.setSessionByToken(token, session);
            }
            return session == null ? null : Convert.convert(SecuritySession.class, session);
        } else {
            Dict dict = Dict.create()
                    .set(LambdaUtil.getFieldName(SecuritySession::getShaToken), token);
            return dbAccess.selectOneByMap(dict, SecuritySession.class);
        }

    }

    @Override
    public SecuritySession saveSession(SecuritySession securitySession) {
        super.saveSession(securitySession);

        if (isClient) {
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(SAVE_SESSION)
                    .body(securitySession)
                    .isJson(true)
                    .build();

            EasyResult<Object> securitySessionEasyResult = easy4jNacosInvokerApi.post(build);
            CheckUtils.checkRpcRes(securitySessionEasyResult);
            SecuritySession securitySession1 = CheckUtils.convertRpcRes(securitySessionEasyResult, SecuritySession.class);
            securityContext.setSession(securitySession1);
            return securitySession1;
        } else {
            int i = dbAccess.saveOne(securitySession, SecuritySession.class);
            if (i > 0) {
                return securitySession;
            }
            return null;
        }

    }

    @Override
    public void deleteSession(String token) {
        if (isClient) {
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(DELETE_SESSION + SP.SLASH + token)
                    .isJson(true)
                    .build();

            EasyResult<Object> securitySessionEasyResult = easy4jNacosInvokerApi.delete(build);
            CheckUtils.checkRpcRes(securitySessionEasyResult);
            securityContext.removeSessionByToken(token);
        } else {
            Dict dict = Dict.create().set(LambdaUtil.getFieldName(SecuritySession::getShaToken), token);
            dbAccess.deleteByMap(dict, SecuritySession.class);
        }

    }

    @Override
    public SecuritySession getSessionByUserName(String userName) {


        if (isClient) {
            ISecurityEasy4jSession o = securityContext.getSession();
            if (o == null || !StrUtil.equals(o.getUserName(), userName)) {
                NacosInvokeDto build = NacosInvokeDto.builder()
                        .group(SysConstant.NACOS_AUTH_GROUP)
                        .serverName(serverName)
                        .path(GET_SESSION_BY_USER_NAME + SP.SLASH + userName)
                        .build();

                EasyResult<Object> securitySessionEasyResult = easy4jNacosInvokerApi.get(build);
                CheckUtils.checkRpcRes(securitySessionEasyResult);
                o = CheckUtils.convertRpcRes(securitySessionEasyResult, SecuritySession.class);
                if (o != null) {
                    securityContext.setSessionByToken(o.getShaToken(), o);
                }
            }
            return Convert.convert(SecuritySession.class, o);

        } else {
            Dict dict = Dict.create()
                    .set(LambdaUtil.getFieldName(SecuritySession::getUserName), userName);
            return dbAccess.selectOneByMap(dict, SecuritySession.class);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecuritySession refreshSession(String token, Integer expireTime, TimeUnit timeUnit) {
        if (isClient) {
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(REFRESH_SESSION + SP.SLASH + token)
                    .build();

            EasyResult<Object> securitySessionEasyResult = easy4jNacosInvokerApi.get(build);
            CheckUtils.checkRpcRes(securitySessionEasyResult);
            SecuritySession securitySession = CheckUtils.convertRpcRes(securitySessionEasyResult, SecuritySession.class);
            if (null != securitySession) {
                securityContext.setSessionByToken(securitySession.getShaToken(), securitySession);
            }
            return securitySession;
        } else {
            return super.refreshSession(token, expireTime, timeUnit);
        }
    }
}
