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
package easy4j.module.sauth.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.user.UserContext;
import easy4j.infra.webmvc.AbstractEasy4JWebMvcHandler;
import easy4j.module.sauth.annotations.OpenApi;
import easy4j.module.sauth.authentication.AuthenticationScopeType;
import easy4j.module.sauth.authentication.AuthenticationType;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.core.Easy4jAuth;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;

import easy4j.module.sauth.domain.SecurityUser;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 获取用户信息
 */
public class Easy4jSecurityFilterInterceptor extends AbstractEasy4JWebMvcHandler {

    SecurityAuthorization authorizationStrategy;

    public SecurityAuthorization getAuthorizationStrategy() {
        if (authorizationStrategy == null) {
            authorizationStrategy = SpringUtil.getBean(SecurityAuthorization.class);
        }
        return authorizationStrategy;
    }


    public Easy4jSecurityFilterInterceptor() {
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) {
        boolean property1 = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
        if (!property1) {
            return true;
        }

        SecurityAuthorization authorizationStrategy1 = getAuthorizationStrategy();
        Method method = handler.getMethod();
        // 开放api授权
        if (method.isAnnotationPresent(OpenApi.class)) {
            OpenApi annotation = method.getAnnotation(OpenApi.class);
            String xApiKey = request.getHeader(SysConstant.X_API_KEY);
            // TODO  api key
        } else {
            // take session
            String token = request.getHeader(SysConstant.X_ACCESS_TOKEN);
            if (!authorizationStrategy1.isNeedLogin(handler, request, response)) {
                return true;
            }
            boolean b1 = authorizationStrategy1.needTakeToken(handler, request, response);
            OnlineUserInfo onlineUserInfo = null;
            if (b1) {
                if (StrUtil.isBlank(token)) {
                    throw EasyException.wrap(BusCode.A00029, SysConstant.X_ACCESS_TOKEN);
                }
                Easy4j.getContext().registerThreadHash(SysConstant.X_ACCESS_TOKEN, SysConstant.X_ACCESS_TOKEN, token);
                String header = request.getHeader(SysConstant.AUTHORIZATION_TYPE);

                SecurityUser securityUser = new SecurityUser();
                securityUser.setShaToken(token);
                securityUser.setScope(AuthenticationScopeType.Interceptor);
                if (StrUtil.isBlank(header) || StrUtil.equals(header, AuthenticationType.ShaToken.name())) {
                    securityUser.setAuthenticationType(AuthenticationType.ShaToken);
                } else if (StrUtil.equals(header, AuthenticationType.Jwt.name())) {
                    securityUser.setAuthenticationType(AuthenticationType.Jwt);
                }
                onlineUserInfo = Easy4jAuth.authentication(securityUser, null);

                ISecurityEasy4jUser user = onlineUserInfo.getUser();
                authorizationStrategy1.checkByUserInfo(user);
                if (
                        StrUtil.isNotBlank(user.getErrorCode())
                ) {
                    throw new EasyException(user.getErrorCode());
                }
                bindUserCtx(user);
                request.setAttribute(SysConstant.SESSION_USER,user);


            } else {
                // 先不做这种功能
                throw EasyException.wrap(BusCode.A00041);
            }
            authorizationStrategy1.authorization(request,onlineUserInfo, handler);
        }
        return true;
    }

    private static void bindUserCtx(ISecurityEasy4jUser user) {
        Easy4jContext context = Easy4j.getContext();
        UserContext userContext = new UserContext();
        userContext.setUserName(user.getUsername());
        userContext.setUserNameCn(user.getUsernameCn());
        context.registerThreadHash(UserContext.USER_CONTEXT_NAME,UserContext.USER_CONTEXT_NAME,userContext);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handler) {
    }

    @Override
    public Integer getOrder() {
        return Integer.MIN_VALUE;
    }
}
