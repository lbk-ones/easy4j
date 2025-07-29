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

import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;


import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * static security impl
 *
 * @author bokun.li
 * @date 2025/6/5
 */
public class Easy4jAuth {
    private static SecurityService bean;

    private static Optional<SecurityService> get() {
        if (bean == null) {
            synchronized (Easy4jAuth.class) {
                bean = SpringUtil.getBean(SecurityService.class);
            }
        }
        return Optional.ofNullable(bean);
    }

    public static OnlineUserInfo getOnlineUser() {
        return get()
                .map(SecurityService::getOnlineUser)
                .orElse(null);
    }

    public static OnlineUserInfo getOnlineUser(String token) {
        return get()
                .map(e -> e.getOnlineUser(token))
                .orElse(null);
    }

    public static boolean isOnline(String token) {
        return get()
                .map(e -> e.isOnline(token))
                .orElse(false);
    }

    public static OnlineUserInfo authentication(ISecurityEasy4jUser securityUser, Consumer<AuthenticationContext> loginAware) {
        return get()
                .map(e -> e.authentication(securityUser, loginAware))
                .orElse(null);
    }

    public static OnlineUserInfo logout() {
        return get()
                .map(SecurityService::logout)
                .orElse(null);
    }

    public static OnlineUserInfo logoutByUserName(String userName) {
        return get()
                .map(e -> e.logoutByUserName(userName))
                .orElse(null);
    }

    public static String getToken() {
        return get().map(SecurityService::getToken).orElse(null);
    }

    public static String refreshToken(int expireTime, TimeUnit timeUnit) {
        return get().map(e -> e.refreshToken(expireTime, timeUnit)).orElse(null);
    }
}
