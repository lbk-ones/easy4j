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

import easy4j.module.sauth.domain.SecurityUserInfo;

import java.util.concurrent.TimeUnit;

/**
 * 核心类
 * 权限接口
 */
public interface SecurityService {
    /**
     * 从上下文中获取当前在线用户
     *
     * @return
     */
    SecurityUserInfo getOnlineUser();

    /**
     * 根据token获取在线用户
     *
     * @param token
     * @return
     */
    SecurityUserInfo getOnlineUser(String token);

    boolean isOnline(String token);

    /**
     * 登录
     */
    SecurityUserInfo login(SecurityUserInfo securityUser);

    SecurityUserInfo logout();

    SecurityUserInfo logoutByUserName(String userName);

    String getToken();

    String refreshToken(int expireTime, TimeUnit timeUnit);

}
