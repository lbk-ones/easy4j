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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.THConstant;
import easy4j.module.sauth.annotations.HasPermission;
import easy4j.module.sauth.annotations.HasRole;
import easy4j.module.sauth.annotations.NoLogin;
import easy4j.module.sauth.annotations.OpenApi;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import easy4j.module.sauth.domain.SecurityAuthority;

import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AbstractAuthorizationStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class AbstractAuthorizationStrategy implements SecurityAuthorization {


    // 默认通过
    @Override
    public void authorization(HttpServletRequest request, OnlineUserInfo securityUserInfo, HandlerMethod handlerMethod) throws EasyException {
        if (securityUserInfo == null) return;
        Set<SecurityAuthority> authorities = securityUserInfo.getAuthorityList();
        if (CollUtil.isNotEmpty(authorities)) {
            authorities = authorities.stream().filter(SecurityAuthority::isEnabled).collect(Collectors.toSet());
            if (CollUtil.isEmpty(authorities)) {
                return;
            }
        } else {
            return;
        }
        Method method = handlerMethod.getMethod();
        Class<?> classType = handlerMethod.getBeanType();
        boolean role = hasRole(authorities, method, classType);
        boolean permission = hasPermission(authorities, method, classType);
        if (!(role && permission)) {
            String message = getMessage(method, classType);
            String requestURI = request.getRequestURI();
            throw EasyException.wrap(message, requestURI);
        }
    }

    private String getMessage(Method method, Class<?> aClass) {
        HasPermission hasPermissionAnnotation = getHasPermissionAnnotation(method, aClass);
        HasRole hasRole = getHasRole(method, aClass);
        String message = BusCode.A00051;
        if (hasRole != null) {
            message = hasRole.message();
        }
        // HasPermission Priority First !!
        if (hasPermissionAnnotation != null) {
            message = hasPermissionAnnotation.message();
        }
        return message;
    }

    private HasPermission getHasPermissionAnnotation(Method method, Class<?> aClass) {
        HasPermission annotation = null;
        if (method.isAnnotationPresent(HasPermission.class)) {
            annotation = method.getAnnotation(HasPermission.class);
            String[] value = annotation.value();
            String[] group = annotation.group();
            if (
                    ListTs.asList(value).stream().allMatch(StrUtil::isBlank) &&
                            ListTs.asList(group).stream().allMatch(StrUtil::isBlank)
            ) {
                if (aClass.isAnnotationPresent(HasPermission.class))
                    annotation = aClass.getAnnotation(HasPermission.class);
            }
        } else {
            if (aClass.isAnnotationPresent(HasPermission.class)) annotation = aClass.getAnnotation(HasPermission.class);
        }
        if (null == annotation) return null;
        String[] value = annotation.value();
        String[] group = annotation.group();
        if (
                ListTs.asList(value).stream().allMatch(StrUtil::isBlank) &&
                        ListTs.asList(group).stream().allMatch(StrUtil::isBlank)
        ) {
            return null;
        }
        return annotation;
    }

    private HasRole getHasRole(Method method, Class<?> aClass) {
        HasRole annotation = null;
        if (method.isAnnotationPresent(HasRole.class)) {
            annotation = method.getAnnotation(HasRole.class);
            String[] value = annotation.value();
            if (
                    ListTs.asList(value).stream().allMatch(StrUtil::isBlank)
            ) {
                if (aClass.isAnnotationPresent(HasRole.class)) annotation = aClass.getAnnotation(HasRole.class);
            }
        } else {
            if (aClass.isAnnotationPresent(HasRole.class)) annotation = aClass.getAnnotation(HasRole.class);
        }
        if (null == annotation) return null;
        String[] value = annotation.value();
        if (
                ListTs.asList(value).stream().allMatch(StrUtil::isBlank)
        ) {
            return null;
        }
        return annotation;
    }

    private boolean hasPermission(Set<SecurityAuthority> authorities, Method method, Class<?> aClass) {
        HasPermission annotation = getHasPermissionAnnotation(method, aClass);
        if (null == annotation) return true;
        String[] value = annotation.value();
        boolean and = annotation.and();
        boolean result = true;
        boolean has = false;
        String[] group = annotation.group();
        // check group
        for (String group2 : group) {
            if (StrUtil.isBlank(group2)) {
                continue;
            }
            for (SecurityAuthority authority : authorities) {
                String groupAuth = authority.getGroup();
                if (StrUtil.equals(group2, groupAuth) && StrUtil.isNotBlank(groupAuth)) {
                    has = true;
                    break;
                }
            }
            if (and) {
                if (!has) {
                    result = false;
                    break;
                }
            }
        }
        if (!and && !has) {
            result = false;
        }
        for (String permissionCode : value) {
            if (StrUtil.isBlank(permissionCode)) {
                continue;
            }
            for (SecurityAuthority authority : authorities) {
                String roleCode = authority.getRoleCode();
                String menuCode = authority.getMenuCode();
                String authorityCode = authority.getAuthorityCode();
                String requestUri = authority.getRequestUri();
                // role
                if (StrUtil.equals(permissionCode, roleCode) && StrUtil.isNotBlank(roleCode)) {
                    has = true;
                    break;
                    // menucode
                } else if (StrUtil.equals(menuCode, permissionCode) && StrUtil.isNotBlank(menuCode)) {
                    has = true;
                    break;
                    // authority code
                } else if (StrUtil.equals(authorityCode, permissionCode) && StrUtil.isNotBlank(authorityCode)) {
                    has = true;
                    break;
                    // requestUri
                } else if (StrUtil.equals(requestUri, permissionCode) && StrUtil.isNotBlank(requestUri)) {
                    has = true;
                    break;
                }
            }
            if (and) {
                if (!has) {
                    result = false;
                    break;
                }
            }
        }
        return result && has;
    }

    private boolean hasRole(Set<SecurityAuthority> authorities, Method method, Class<?> aClass) {
        HasRole annotation = getHasRole(method, aClass);
        if (null == annotation) return true;
        String[] value = annotation.value();
        boolean and = annotation.and();
        boolean result = true;
        boolean has = false;
        for (String roleCode : value) {
            if (StrUtil.isBlank(roleCode)) {
                continue;
            }
            for (SecurityAuthority authority : authorities) {
                String roleCode1 = authority.getRoleCode();
                if (StrUtil.equals(roleCode, roleCode1) && StrUtil.isNotBlank(roleCode1)) {
                    has = true;
                    break;
                }
            }
            if (and) {
                if (!has) {
                    result = false;
                    break;
                }
            }
        }
        if (!and && !has) {
            result = false;
        }
        return result;
    }

    @Override
    public boolean isNeedLogin(HandlerMethod handlerMethod, HttpServletRequest httpServerRequest, HttpServletResponse httpServerResponse) {


        Method method = handlerMethod.getMethod();
        Class<?> beanType = handlerMethod.getBeanType();

        // 外部接口直接跳过，可以指定权限扫描路径
        String packageNme = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_SCAN_PACKAGE_PREFIX);
        packageNme = StrUtil.isBlank(packageNme) ? Easy4j.mainClassPath : packageNme;
        String name = beanType.getName();

        if (ListTs.asList(packageNme.split(SP.COMMA)).stream().noneMatch(name::startsWith) && !StrUtil.startWith(name, SysConstant.PACKAGE_PREFIX + SP.DOT))
            return false;

        boolean classNoLogin = beanType.isAnnotationPresent(NoLogin.class) && beanType.getAnnotation(NoLogin.class).rpcNoLogin();
        boolean methodNoLogin = method.isAnnotationPresent(NoLogin.class) && method.getAnnotation(NoLogin.class).rpcNoLogin();
        // rpc 传递
        boolean noLoginRpc = StrUtil.equals(httpServerRequest.getHeader(THConstant.EASY4J_RPC_NO_LOGIN), "1");

        // 是否是免登录的 供后续模块使用
        boolean isNoLogin = beanType.isAnnotationPresent(NoLogin.class) || method.isAnnotationPresent(NoLogin.class) || noLoginRpc;
        getContext().registerThreadHash(THConstant.EASY4J_IS_NO_LOGIN, THConstant.EASY4J_IS_NO_LOGIN, isNoLogin);
        if (classNoLogin || methodNoLogin || noLoginRpc) {
            getContext().registerThreadHash(THConstant.EASY4J_RPC_NO_LOGIN, THConstant.EASY4J_RPC_NO_LOGIN, "1");
            return false;
        }
        // 其他全部需要登录
        return true;
    }

    private Easy4jContext getContext() {
        return Easy4j.getContext();
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
    public void checkByUserInfo(ISecurityEasy4jUser securityUserInfo) {

    }
}
