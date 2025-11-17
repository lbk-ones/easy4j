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

import easy4j.infra.common.annotations.Desc;
import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 核心类
 * 权限接口
 * 用户信息或i去
 * token获取
 * 是否在线
 * 登录
 * 登出
 * 根据用户名退出
 * 获取当前用户登录token
 * token刷新
 * 权限获取
 */
public interface SecurityService {
    /**
     * 从上下文中获取当前在线用户
     *
     * @return
     */
    @Desc("从上下文中获取当前在线用户")
    OnlineUserInfo getOnlineUser();

    /**
     * 根据token获取在线用户
     *
     * @param token
     * @return
     */
    @Desc("根据token获取在线用户")
    OnlineUserInfo getOnlineUser(String token);

    /**
     * 是否在线
     *
     * @param token
     * @return
     */
    @Desc("是否在线")
    boolean isOnline(String token);

    /**
     * 认证
     *
     * @param securityUser 传过来的用户信息
     * @param loginAware   登录成功之后的回调
     * @return
     */
    @Desc("认证")
    OnlineUserInfo authentication(ISecurityEasy4jUser securityUser, Consumer<AuthenticationContext> loginAware);

    /**
     * 退出登录
     *
     * @return
     */
    @Desc("退出登录")
    OnlineUserInfo logout();

    /**
     * 根据用户名退出
     *
     * @param userName
     * @return
     */
    @Desc("根据用户名退出")
    OnlineUserInfo logoutByUserName(String userName);

    /**
     * 获取当前用户登录token
     *
     * @return
     */
    @Desc("获取当前用户登录token")
    String getToken();

    /**
     * token刷新
     *
     * @param expireTime
     * @param timeUnit
     * @return
     */
    @Desc("token刷新")
    String refreshToken(int expireTime, TimeUnit timeUnit);

}
