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
package easy4j.module.sauth.authorization;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.SysConstant;
import easy4j.module.sauth.annotations.NoLogin;
import easy4j.module.sauth.annotations.OpenApi;
import easy4j.module.sauth.domain.SecurityAuthority;
import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

/**
 * AbstractAuthorizationStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class AbstractAuthorizationStrategy implements SecurityAuthorization {


    @Override
    public Set<SecurityAuthority> getAuthorizationByUsername(String userName) {
        return null;
    }

    // 默认通过
    @Override
    public void customAuthenticationByMethod(SecurityUserInfo securityUserInfo, HandlerMethod handlerMethod) throws EasyException {
        Set<SecurityAuthority> authorities = securityUserInfo.getAuthorities();
        Method method = handlerMethod.getMethod();
        NoLogin annotation = method.getAnnotation(NoLogin.class);
        if (Objects.nonNull(annotation)) {

        }
    }

    @Override
    public boolean isNeedAuthentication(SecurityUserInfo userInfo, Set<SecurityAuthority> authorities, HttpServerRequest request, HttpServerResponse response) {
        return false;
    }

    @Override
    public boolean checkUri(HandlerMethod handlerMethod) {
        return false;
    }

    @Override
    public boolean isNeedLogin(HandlerMethod handlerMethod, HttpServletRequest httpServerRequest, HttpServletResponse httpServerResponse) {
        Method method = handlerMethod.getMethod();
        Class<?> beanType = handlerMethod.getBeanType();
        boolean annotationPresent1 = beanType.isAnnotationPresent(NoLogin.class);
        if (annotationPresent1) {
            return false;
        }
        return !method.isAnnotationPresent(NoLogin.class);
    }

    @Override
    public boolean needTakeToken(HandlerMethod handlerMethod, HttpServletRequest httpServerRequest, HttpServletResponse httpServerResponse) {
        String header = httpServerRequest.getHeader(SysConstant.EASY4J_NO_NEED_TOKEN);
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(OpenApi.class)) {
            return false;
        }
        return !StrUtil.equals(header, "1");
    }

    @Override
    public boolean checkByUserInfo(SecurityUserInfo securityUserInfo) {
        return true;
    }
}
