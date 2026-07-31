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
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import easy4j.infra.dbaccess.OrmInternal;
import easy4j.infra.dbaccess.orm.IDBAccess;
import easy4j.infra.dbaccess.orm.conditions.FWhereBuild;
import easy4j.module.sauth.config.Config;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.NacosInvokerApi;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.SecuritySession;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.transaction.annotation.Transactional;

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

    private static IDBAccess dbAccess;

    @Resource
    SecurityContext securityContext;

    boolean isClient;

    String serverName;
    boolean sAuthEnable;
    boolean isServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        dbAccess = OrmInternal.getNoTransactionOrm();
        sAuthEnable = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
        isServer = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_SERVER, boolean.class);
        if (!isServer && sAuthEnable) {
            serverName = Config.AUTH_SERVER_NAME;
            isClient = true;
        }
        // server run session clear thread
        if (isServer && sAuthEnable) {
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
            if (!sAuthEnable) {
                log.error(SysLog.compact("auth is not enable!"));
                throw EasyException.wrap(BusCode.A000031, "auth is not enable!");
            }
            // cache
            ISecurityEasy4jSession session = securityContext.getSessionByToken(token);
            if (session == null) {
                NacosInvokeDto build = NacosInvokeDto.builder()
                        .group(SysConstant.NACOS_AUTH_GROUP)
                        .serverName(serverName)
                        .accessToken(token)
                        .path(GET_SESSION + SP.SLASH + token)
                        .build();

                String res = NacosInvokerApi.getEasy4jNacosInvokerApi().get(build);

                EasyResult<SecuritySession> object = JacksonUtil.toObject(res, new TypeReference<EasyResult<SecuritySession>>() {
                });
                CheckUtils.checkRpcRes(object);
                securityContext.setSessionByToken(token, object.getData());
            }
            return session == null ? null : Convert.convert(SecuritySession.class, session);
        } else {
            ISecurityEasy4jSession session = securityContext.getSessionByToken(token);
            if(session == null){
                FWhereBuild<SecuritySession> eq = FWhereBuild.get(SecuritySession.class).eq(SecuritySession::getShaToken, token);
                SecuritySession securitySession = dbAccess.queryOne(eq, SecuritySession.class);
                securityContext.setSessionByToken(token,securitySession);
                return securitySession;
            }else{
                return Convert.convert(SecuritySession.class,session);
            }
        }

    }

    @Override
    public SecuritySession saveSession(SecuritySession securitySession) {
        if (isClient) {
            if (!sAuthEnable) {
                log.error(SysLog.compact("auth is not enable!"));
                throw EasyException.wrap(BusCode.A000031, "auth is not enable!");
            }
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(SAVE_SESSION)
                    .body(securitySession)
                    .isJson(true)
                    .build();

            String res = NacosInvokerApi.getEasy4jNacosInvokerApi().post(build);
            EasyResult<SecuritySession> object = JacksonUtil.toObject(res, new TypeReference<EasyResult<SecuritySession>>() {
            });
            CheckUtils.checkRpcRes(object);
            SecuritySession data = object.getData();
            securityContext.setSession(data);
            if (null != data) {
                securityContext.setSessionByToken(data.getShaToken(), data);
            }
            return data;
        } else {
            super.saveSession(securitySession);
            SecuritySession save = dbAccess.save(securitySession, SecuritySession.class);
            if (save!=null) {
                return securitySession;
            }
            return null;
        }

    }


    @Override
    public void deleteSession(String token) {
        if (isClient) {
            if (!sAuthEnable) {
                log.error(SysLog.compact("auth is not enable!"));
                throw EasyException.wrap(BusCode.A000031, "auth is not enable!");
            }
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(DELETE_SESSION + SP.SLASH + token)
                    .isJson(true)
                    .build();

            String res = NacosInvokerApi.getEasy4jNacosInvokerApi().delete(build);
            EasyResult<Object> object = JacksonUtil.toObject(res, new TypeReference<EasyResult<Object>>() {
            });
            CheckUtils.checkRpcRes(object);
            securityContext.removeSessionByToken(token);
            ISecurityEasy4jSession session = securityContext.getSession();
            if (session != null && StrUtil.equals(session.getShaToken(), token)) {
                securityContext.removeSession();
            }
        } else {
            FWhereBuild<SecuritySession> eq = FWhereBuild.get(SecuritySession.class).eq(SecuritySession::getShaToken, token);
            dbAccess.delete(eq, SecuritySession.class);
        }

    }

    @Override
    public SecuritySession getSessionByUserName(String userName) {


        if (isClient) {
            if (!sAuthEnable) {
                log.error(SysLog.compact("auth is not enable!"));
                throw EasyException.wrap(BusCode.A000031, "auth is not enable!");
            }
            ISecurityEasy4jSession o = securityContext.getSession();
            if (o == null || !StrUtil.equals(o.getUsername(), userName)) {
                NacosInvokeDto build = NacosInvokeDto.builder()
                        .group(SysConstant.NACOS_AUTH_GROUP)
                        .serverName(serverName)
                        .path(GET_SESSION_BY_USER_NAME + SP.SLASH + userName)
                        .build();

                String res = NacosInvokerApi.getEasy4jNacosInvokerApi().get(build);
                EasyResult<SecuritySession> object = JacksonUtil.toObject(res, new TypeReference<EasyResult<SecuritySession>>() {
                });
                CheckUtils.checkRpcRes(object);
                SecuritySession data = object.getData();
                if (data != null) {
                    securityContext.setSessionByToken(data.getShaToken(), data);
                }
            }
            return Convert.convert(SecuritySession.class, o);

        } else {
            ISecurityEasy4jSession o = securityContext.getSession();
            if (o == null) {
                FWhereBuild<SecuritySession> eq = FWhereBuild.get(SecuritySession.class).eq(SecuritySession::getUsername, userName);
                SecuritySession securitySession = dbAccess.queryOne(eq, SecuritySession.class);
                securityContext.setSession(securitySession);
                return securitySession;
            }else if(StrUtil.equals(o.getUsername(),userName)){
                return Convert.convert(SecuritySession.class, o);
            }else{
                FWhereBuild<SecuritySession> eq = FWhereBuild.get(SecuritySession.class).eq(SecuritySession::getUsername, userName);
                return dbAccess.queryOne(eq, SecuritySession.class);
            }

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecuritySession refreshSession(String token, Integer expireTime, TimeUnit timeUnit) {
        if (isClient) {
            if (!sAuthEnable) {
                log.error(SysLog.compact("auth is not enable!"));
                throw EasyException.wrap(BusCode.A000031, "auth is not enable!");
            }
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(REFRESH_SESSION + SP.SLASH + token)
                    .build();

            String res = NacosInvokerApi.getEasy4jNacosInvokerApi().get(build);
            EasyResult<SecuritySession> object = JacksonUtil.toObject(res, new TypeReference<EasyResult<SecuritySession>>() {
            });
            CheckUtils.checkRpcRes(object);
            SecuritySession data = object.getData();
            if (null != data) {
                securityContext.setSessionByToken(data.getShaToken(), data);
            }
            return data;
        } else {
            return super.refreshSession(token, expireTime, timeUnit);
        }
    }
}
