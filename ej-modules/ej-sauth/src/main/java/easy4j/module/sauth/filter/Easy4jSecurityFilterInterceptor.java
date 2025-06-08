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
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.webmvc.AbstractEasy4JWebMvcHandler;
import easy4j.module.sauth.annotations.OpenApi;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 获取用户信息
 */
public class Easy4jSecurityFilterInterceptor extends AbstractEasy4JWebMvcHandler {

    SessionStrategy sessionStrategy;


    SecurityContext securityContext;

    SecurityAuthorization authorizationStrategy;


    SecurityAuthentication securityAuthentication;

    public Easy4jSecurityFilterInterceptor(SessionStrategy sessionStrategy, SecurityContext securityContext, SecurityAuthorization authorizationStrategy, SecurityAuthentication securityAuthentication) {
        this.sessionStrategy = sessionStrategy;
        this.securityContext = securityContext;
        this.authorizationStrategy = authorizationStrategy;
        this.securityAuthentication = securityAuthentication;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) {
        Method method = handler.getMethod();
        // 开放api授权
        if (method.isAnnotationPresent(OpenApi.class)) {
            OpenApi annotation = method.getAnnotation(OpenApi.class);
            String xApiKey = request.getHeader(SysConstant.X_API_KEY);
            // TODO  api key
        } else {
            // take session
            String token = request.getHeader(SysConstant.X_ACCESS_TOKEN);

            boolean b1 = authorizationStrategy.needTakeToken(handler, request, response);
            SecurityUserInfo securityUserInfo = null;
            if (b1) {
                if (StrUtil.isBlank(token)) {
                    throw EasyException.wrap(BusCode.A00029, SysConstant.X_ACCESS_TOKEN);
                }
                securityUserInfo = securityAuthentication.tokenAuthentication(token);
                if (
                        StrUtil.isNotBlank(securityUserInfo.getErrorCode())
                ) {
                    throw new EasyException(securityUserInfo.getErrorCode());
                }
            } else {
                // 先不做这种功能
                throw EasyException.wrap(BusCode.A00041);
            }
            authorizationStrategy.customAuthenticationByMethod(securityUserInfo, handler);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handler) {
    }
}
