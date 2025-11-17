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

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.ServiceLoaderUtils;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.user.UserContext;
import easy4j.infra.webmvc.AbstractEasy4JWebMvcHandler;
import easy4j.module.sauth.annotations.OpenApi;
import easy4j.module.sauth.authentication.AuthenticationScopeType;
import easy4j.module.sauth.authentication.AuthenticationType;
import easy4j.module.sauth.authentication.IBearerAuthentication;
import easy4j.module.sauth.authentication.LoadAuthentication;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.core.Easy4jAuth;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import easy4j.module.sauth.domain.SecurityUser;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

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
        Class<?> beanType = handler.getBeanType();
        // 开放api授权
        if (method.isAnnotationPresent(OpenApi.class) || beanType.isAnnotationPresent(OpenApi.class)) {
            handlerOpenApi(request, method, beanType, authorizationStrategy1);
        } else {
            // take session
            String token = StrUtil.blankToDefault(request.getHeader(SysConstant.X_ACCESS_TOKEN), request.getParameter(SysConstant.X_ACCESS_TOKEN));
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
                    securityUser.setAuthenticationType(AuthenticationType.ShaToken.name());
                } else if (StrUtil.equals(header, AuthenticationType.Jwt.name())) {
                    securityUser.setAuthenticationType(AuthenticationType.Jwt.name());
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
                request.setAttribute(SysConstant.SESSION_USER, user);
            } else {
                // 先不做这种功能
                throw EasyException.wrap(BusCode.A00041);
            }
            authorizationStrategy1.authorization(request, onlineUserInfo, handler);
        }
        return true;
    }

    /**
     * 处理 OpenApi类型
     *
     * @param request                http请求体对象
     * @param method                 方法
     * @param beanType               bean class对象
     * @param authorizationStrategy1 权限实现
     * @author bokun.li
     * @date 2025/11/14
     */
    private void handlerOpenApi(HttpServletRequest request, Method method, Class<?> beanType, SecurityAuthorization authorizationStrategy1) {
        OpenApi annotation = method.getAnnotation(OpenApi.class);
        if (annotation == null) {
            annotation = beanType.getAnnotation(OpenApi.class);
        }
        String tokenHeaderName = annotation.tokenHeaderName();
        AuthenticationType authenticationType = annotation.authenticationType();
        // headerValue 可能是 Basic Token 也可能是 Jwt Token 也可能是 Bearer Token 也可能是 Sha Token
        String headerValue = request.getHeader(tokenHeaderName);
        SecurityUser securityUser = new SecurityUser();
        securityUser.setShaToken(headerValue);
        securityUser.setAuthenticationType(authenticationType.name());
        securityUser.setScope(AuthenticationScopeType.Interceptor);
        handlerAccessToken(annotation, securityUser);
        if (AuthenticationType.BearerToken.name().equals(securityUser.getAuthenticationType())) {
            Class<? extends IBearerAuthentication> aClass = annotation.bearerImpl();
            IBearerAuthentication iBearerAuthentication = getInstance(aClass);
            securityUser.setBearerAuthentication(iBearerAuthentication);
        } else if (AuthenticationType.Other.name().equals(securityUser.getAuthenticationType())) {
            Class<? extends LoadAuthentication> aClass = annotation.otherImpl();
            LoadAuthentication iBearerAuthentication = getInstance(aClass);
            securityUser.setLoadAuthentication(iBearerAuthentication);
        }
        OnlineUserInfo onlineUserInfo = Easy4jAuth.authentication(securityUser, null);
        ISecurityEasy4jUser user = onlineUserInfo.getUser();
        authorizationStrategy1.checkByUserInfo(user);
        if (
                StrUtil.isNotBlank(user.getErrorCode())
        ) {
            throw new EasyException(user.getErrorCode());
        }
        bindUserCtx(user);
        request.setAttribute(SysConstant.SESSION_USER, user);
    }

    @Nullable
    private <T> T getInstance(Class<T> aClass) {
        T iBearerAuthentication = null;
        try{
            iBearerAuthentication = ReflectUtil.newInstance(aClass);
        }catch (Throwable ignored){}
        if(iBearerAuthentication == null){
            List<T> load = ServiceLoaderUtils.load(aClass);
            if (ListTs.isEmpty(load)) {
                try{
                    iBearerAuthentication = SpringUtil.getBean(aClass);
                }catch (Exception ignored){}
            }else{
                iBearerAuthentication = ListTs.get(load,0);
            }
        }
        return iBearerAuthentication;
    }

    /**
     * 兼容获取AccessToken的值
     * 注解上的值
     * 环境变量
     * spring配置值
     *
     * @param annotation
     * @param securityUser
     */
    private static void handlerAccessToken(OpenApi annotation, SecurityUser securityUser) {
        String at = annotation.accessToken();
        if (StrUtil.isNotBlank(at)) {
            if (StrUtil.startWith(at, "$")) {
                String envName = at.substring(1);
                if (StrUtil.isNotBlank(envName)) {
                    at = System.getenv(envName);
                    securityUser.setAccessToken(at);
                }
            } else {
                String pAt = Easy4j.getProperty(at);
                if (StrUtil.isNotBlank(pAt)) {
                    securityUser.setAccessToken(pAt);
                }
            }
            if (StrUtil.isNotBlank(securityUser.getAccessToken())) {
                securityUser.setAuthenticationType(AuthenticationType.AccessToken.name());
            }
        }
    }

    private static void bindUserCtx(ISecurityEasy4jUser user) {
        Easy4jContext context = Easy4j.getContext();
        UserContext userContext = new UserContext();
        userContext.setUserName(user.getUsername());
        userContext.setUserNameCn(user.getUsernameCn());
        context.registerThreadHash(UserContext.USER_CONTEXT_NAME, UserContext.USER_CONTEXT_NAME, userContext);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handler) {
    }

    @Override
    public Integer getOrder() {
        return Integer.MIN_VALUE;
    }
}
