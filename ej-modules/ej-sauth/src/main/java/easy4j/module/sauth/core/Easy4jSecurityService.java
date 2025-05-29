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
package easy4j.module.sauth.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.utils.BusCode;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;


/**
 * Easy4jSecurityService
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Easy4jSecurityService extends AbstractSecurityService implements InitializingBean {


    SecurityAuthentication securityAuthentication;


    SessionStrategy sessionStrategy;
    SecurityContext securityContext;


    SecurityAuthorization authorizationStrategy;

    public Easy4jSecurityService(
            SecurityAuthentication securityAuthentication,
            SessionStrategy sessionStrategy,
            SecurityAuthorization authorizationStrategy,
            SecurityContext securityContext
    ) {
        this.securityAuthentication = securityAuthentication;
        this.sessionStrategy = sessionStrategy;
        this.authorizationStrategy = authorizationStrategy;
        this.securityContext = securityContext;
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    private static DBAccess dbAccess;

    @Override
    public void afterPropertiesSet() throws Exception {

        dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class));
    }

    @Override
    public SecurityUserInfo getOnlineUser() {
        return null;
    }

    @Override
    public SessionStrategy getSessionStrategy() {
        return sessionStrategy;
    }

    @Override
    public SecurityAuthorization getAuthorizationStrategy() {
        return authorizationStrategy;
    }

    @Override
    public SecurityUserInfo getOnlineUser(String token) {
        return null;
    }


    @Override
    public SecurityUserInfo login(SecurityUserInfo securityUser) {
        SecurityUserInfo securityUserInfo = securityAuthentication.verifyLoginAuthentication(securityUser);
        String errorCode = securityUserInfo.getErrorCode();
        if (StrUtil.isNotBlank(errorCode)) {
            throw new EasyException(errorCode);
        }
        if (!securityAuthentication.checkUser(securityUser)) {
            throw new EasyException(BusCode.A00036);
        }
        SecuritySession init = new SecuritySession().init(securityUser);
        saveSession(init);
        securityUserInfo.setPassword(null);
        return securityUserInfo;
    }

    /**
     * 几种选择
     * 1、存入数据库
     * 2、存入redis
     * 3、存入内存
     *
     * @param init
     */
    private void saveSession(SecuritySession init) {

    }

    @Override
    public boolean isOnline(String token) {
        return false;
    }

    @Override
    public SecurityUserInfo logout() {
        return null;
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public String refreshToken(int expireTime, TimeUnit timeUnit) {
        return null;
    }
}
