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
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.domain.*;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Consumer;


/**
 * Easy4jSecurityService
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Easy4jSecurityService extends AbstractSecurityService {


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


    @Override
    public SessionStrategy getSessionStrategy() {
        return sessionStrategy;
    }

    @Override
    public SecurityAuthorization getAuthorizationStrategy() {
        return authorizationStrategy;
    }

    public HttpServletRequest getServletRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        assert sra != null;
        return (HttpServletRequest) sra.getRequest();
    }


    public ISecurityEasy4jUser verifyPre(ISecurityEasy4jUser user) {
        if (null == user) {
            user = new SecurityUser();
            user.setErrorCode(BusCode.A00004 + ",user");
            return user;
        }
        HttpServletRequest servletRequest = getServletRequest();
        String method = servletRequest.getMethod();
        if (!"post".equalsIgnoreCase(method)) {
            user.setErrorCode(BusCode.A00030);
            return user;
        }
        String username = user.getUsername();
        String password = user.getPassword();
        if (StrUtil.isBlank(username)) {
            user.setErrorCode(BusCode.A00031);
            return user;
        }
        boolean isSkip = user.isSkipPassword();
        if (StrUtil.isBlank(password) && !isSkip) {
            user.setErrorCode(BusCode.A00032);
            return user;
        }
        return user;
    }


    @Override
    public OnlineUserInfo login(ISecurityEasy4jUser securityUser, Consumer<ISecurityEasy4jUser> loginAware) {

        ISecurityEasy4jUser iSecurityEasy4jUser = verifyPre(securityUser);
        String username = securityUser.getUsername();
        if (StrUtil.isNotBlank(iSecurityEasy4jUser.getErrorCode()))
            throw new EasyException(iSecurityEasy4jUser.getErrorCode());


        // 1、first query user info
        ISecurityEasy4jUser dbUser = LoadUserApi.getByUserName(username);

        ISecurityEasy4jUser securityUserInfo = securityAuthentication.verifyLoginAuthentication(securityUser, dbUser);

        if (StrUtil.isNotBlank(securityUserInfo.getErrorCode()))
            throw new EasyException(securityUserInfo.getErrorCode());

        if (!securityAuthentication.checkUser(securityUser)) {
            throw new EasyException(BusCode.A00036);
        }
        SecuritySession init = new SecuritySession().init(securityUser);
        saveSession(init);
        securityUserInfo.setPassword(null);
        securityUserInfo.setShaToken(init.getShaToken());

        if (null != loginAware) {
            loginAware.accept(securityUserInfo);
        }

        bindCtx(init);

        OnlineUserInfo onlineUserInfo = new OnlineUserInfo(init, dbUser);
        onlineUserInfo.handlerAuthorityList(username);
        return onlineUserInfo;
    }

    private void bindCtx(SecuritySession init) {
        SecurityContext securityContext1 = getSecurityContext();
        securityContext1.setSession(init);
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
