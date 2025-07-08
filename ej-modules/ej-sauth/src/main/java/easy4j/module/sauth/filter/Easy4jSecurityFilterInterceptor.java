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
import easy4j.infra.webmvc.AbstractEasy4JWebMvcHandler;
import easy4j.module.sauth.annotations.OpenApi;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 获取用户信息
 */
public class Easy4jSecurityFilterInterceptor extends AbstractEasy4JWebMvcHandler {

    SecurityAuthorization authorizationStrategy;


    SecurityAuthentication securityAuthentication;

    public SecurityAuthorization getAuthorizationStrategy() {
        if (authorizationStrategy == null) {
            authorizationStrategy = SpringUtil.getBean(SecurityAuthorization.class);
        }
        return authorizationStrategy;
    }

    public SecurityAuthentication getSecurityAuthentication() {
        if (securityAuthentication == null) {
            securityAuthentication = SpringUtil.getBean(SecurityAuthentication.class);
        }
        return securityAuthentication;
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
        SecurityAuthentication securityAuthentication1 = getSecurityAuthentication();
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
            SecurityUserInfo securityUserInfo = null;
            if (b1) {
                if (StrUtil.isBlank(token)) {
                    throw EasyException.wrap(BusCode.A00029, SysConstant.X_ACCESS_TOKEN);
                }
                Easy4j.getContext().registerThreadHash(SysConstant.X_ACCESS_TOKEN, SysConstant.X_ACCESS_TOKEN, token);
                securityUserInfo = securityAuthentication1.tokenAuthentication(token);
                if (
                        StrUtil.isNotBlank(securityUserInfo.getErrorCode())
                ) {
                    throw new EasyException(securityUserInfo.getErrorCode());
                }
            } else {
                // 先不做这种功能
                throw EasyException.wrap(BusCode.A00041);
            }
            authorizationStrategy1.customAuthenticationByMethod(securityUserInfo, handler);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handler) {
    }

    @Override
    public Integer getOrder() {
        return Integer.MIN_VALUE;
    }
}
