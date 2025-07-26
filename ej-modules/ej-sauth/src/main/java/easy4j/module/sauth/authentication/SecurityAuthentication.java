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
package easy4j.module.sauth.authentication;

import easy4j.infra.common.exception.EasyException;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;

/**
 * 登录权限认证
 * token认证
 * 查询用户信息
 */
public interface SecurityAuthentication {

    /**
     * 权限认证
     *
     * @param user
     * @return
     */
    ISecurityEasy4jUser verifyLoginAuthentication(ISecurityEasy4jUser user, ISecurityEasy4jUser userByUserName) throws EasyException;

    /**
     * token授权 给登录之后拦截器使用
     *
     * @param token
     * @return
     * @throws EasyException
     */
    OnlineUserInfo tokenAuthentication(String token) throws EasyException;

    /**
     * 权限检查 (黑白名单，是否过期，是否锁定，用户是否存在)
     *
     * @return
     */
    boolean checkUser(ISecurityEasy4jUser user) throws EasyException;

}
