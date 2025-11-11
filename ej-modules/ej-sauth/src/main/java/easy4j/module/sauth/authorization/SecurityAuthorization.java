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

import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import easy4j.infra.common.exception.EasyException;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import easy4j.module.sauth.domain.SecurityAuthority;

import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Set;

/**
 * 授权相关
 */
public interface SecurityAuthorization {

    /**
     * 自定义鉴权根据方法来拦截是否该过
     *
     * @param request
     * @param handlerMethod
     */
    void authorization(HttpServletRequest request, OnlineUserInfo securityUserInfo, HandlerMethod handlerMethod) throws EasyException;

    /**
     * 是否需要登录
     *
     * @param handlerMethod
     * @return
     */
    boolean isNeedLogin(HandlerMethod handlerMethod, HttpServletRequest httpServerRequest, HttpServletResponse httpServerResponse);


    /**
     * 是否应该携带token
     *
     * @param handlerMethod
     * @param httpServerRequest
     * @param httpServerResponse
     * @return
     */
    boolean needTakeToken(HandlerMethod handlerMethod, HttpServletRequest httpServerRequest, HttpServletResponse httpServerResponse);

    /**
     * 根据用户信息来过滤
     * 通过 setErrorCode 或者抛出异常的方式来 处理异常
     *
     * @param securityUserInfo
     * @return
     */
    void checkByUserInfo(ISecurityEasy4jUser securityUserInfo);

}
