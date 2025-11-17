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

import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.authentication.AuthenticationCore;
import easy4j.module.sauth.authentication.AuthenticationFactory;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.session.SessionStrategy;

import java.util.function.Consumer;


/**
 * Easy4jSecurityService
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Easy4jSecurityService extends AbstractSecurityService {


    SessionStrategy sessionStrategy;
    SecurityContext securityContext;


    SecurityAuthorization authorizationStrategy;

    public Easy4jSecurityService(
            SessionStrategy sessionStrategy,
            SecurityAuthorization authorizationStrategy,
            SecurityContext securityContext
    ) {
        this.sessionStrategy = sessionStrategy;
        this.authorizationStrategy = authorizationStrategy;
        this.securityContext = securityContext;
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
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
    public OnlineUserInfo authentication(ISecurityEasy4jUser securityUser, Consumer<AuthenticationContext> loginAware) {

        AuthenticationCore authenticationCore = AuthenticationFactory.get(securityUser.getAuthenticationType());
        AuthenticationContext ctx = AuthenticationFactory.ctx(securityUser);
        ctx.setCheckSession(securityUser.getCheckSession() == null || securityUser.getCheckSession());
        // querySession from db/redis
        authenticationCore.querySession(ctx);
        ctx.checkError();
        // queryUserInfo from db
        authenticationCore.queryUser(ctx);
        ctx.checkError();

        // pre verify
        authenticationCore.verifyPre(ctx);
        ctx.checkError();

        // verify
        authenticationCore.verify(ctx);
        ctx.checkError();

        // checkUser
        if (!authenticationCore.checkUser(ctx)) {
            ctx.checkError(BusCode.A00036);
        }
        // if query session is null,then gen session and dynamicSave
        ISecurityEasy4jSession dbReqSession = ctx.getDbSession();
        if (null == dbReqSession) {
            SecuritySession init = new SecuritySession().init(securityUser);
            saveSession(init);
            ctx.setDbSession(init);
        }
        // refresh session
        authenticationCore.refreshSession(ctx);
        ctx.checkError();

        //  genOnlineUserInfo
        OnlineUserInfo onlineUserInfo = authenticationCore.genOnlineUserInfo(ctx);
        ctx.checkError();

        // bind ctx
        authenticationCore.bindSessionToCtx(ctx);
        ctx.checkError();
        // authentication aware
        if (null != loginAware) {
            loginAware.accept(ctx);
        }
        return onlineUserInfo;
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
        SessionStrategy sessionStrategy1 = getSessionStrategy();
        sessionStrategy1.saveSession(init);
    }
}
