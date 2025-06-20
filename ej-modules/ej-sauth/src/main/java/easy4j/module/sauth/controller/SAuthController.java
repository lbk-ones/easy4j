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
package easy4j.module.sauth.controller;

import cn.hutool.core.collection.CollUtil;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.condition.FWhereBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.module.sauth.core.Easy4jAuth;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 供内部调用
 *
 * @author bokun.li
 * @date 2025-06-18
 */
@RestController
@RequestMapping("sauth")
public class SAuthController {

    @Autowired
    DBAccess dbAccess;

    @Autowired
    SessionStrategy sessionStrategy;

    @GetMapping("getOnlineUserByToken/{token}")
    public EasyResult<Object> getOnlineUserByToken(@PathVariable(name = "token") String token) {
        SecurityUserInfo onlineUser = Easy4jAuth.getOnlineUser(token);
        return EasyResult.ok(onlineUser);
    }

    @GetMapping("isOnline")
    public EasyResult<Object> isOnline() {
        String token1 = Easy4jAuth.getToken();
        boolean isOnline = Easy4jAuth.isOnline(token1);
        return EasyResult.ok(isOnline);
    }

    @GetMapping("getToken")
    public EasyResult<Object> getToken() {
        String token = Easy4jAuth.getToken();
        return EasyResult.ok(token);
    }

    @GetMapping("refreshToken")
    public EasyResult<Object> refreshToken() {
        String token = Easy4jAuth.refreshToken(30, TimeUnit.MINUTES);
        return EasyResult.ok(token);
    }

    @GetMapping("getOnlineUserInfo")
    public EasyResult<Object> getOnlineUserInfo() {
        SecurityUserInfo onlineUser = Easy4jAuth.getOnlineUser();
        return EasyResult.ok(onlineUser);
    }

    @GetMapping("logOut")
    public EasyResult<Object> logOut() {
        SecurityUserInfo onlineUser = Easy4jAuth.logout();
        return EasyResult.ok(onlineUser);
    }

    @GetMapping("getSession/{token}")
    public EasyResult<SecuritySession> getSession(@PathVariable(name = "token") String token) {
        SecuritySession session = sessionStrategy.getSession(token);
        return EasyResult.ok(session);
    }

    @PostMapping("saveSession")
    public EasyResult<Object> saveSession(@RequestBody SecuritySession securitySession) {
        SecuritySession securitySession1 = sessionStrategy.saveSession(securitySession);
        return EasyResult.ok(securitySession1);
    }

    @DeleteMapping("deleteSession/{token}")
    public EasyResult<Object> deleteSession(@PathVariable(name = "token") String token) {
        sessionStrategy.deleteSession(token);
        return EasyResult.ok(null);
    }

    @GetMapping("getSessionByUserName/{username}")
    public EasyResult<SecuritySession> getSessionByUserName(@PathVariable(name = "username") String username) {
        SecuritySession sessionByUserName = sessionStrategy.getSessionByUserName(username);
        return EasyResult.ok(sessionByUserName);
    }

    @GetMapping("loadUserByUserName/{username}")
    public EasyResult<Object> loadUserByUserName(@PathVariable(name = "username") String username) {
        WhereBuild equal = FWhereBuild.get(SecurityUser.class).equal(SecurityUser::getUsername, username);
        List<SecurityUser> securityUsers = dbAccess.selectByCondition(equal, SecurityUser.class);
        if (CollUtil.isNotEmpty(securityUsers)) {
            SecurityUser securityUser = securityUsers.get(0);
            return EasyResult.ok(securityUser.toSecurityUserInfo());
        }
        return EasyResult.ok(null);
    }

    @GetMapping("refreshSession/{token}")
    public EasyResult<Object> refreshSession(@PathVariable(name = "token") String token) {
        EjSysProperties ejSysProperties2 = Easy4j.getEjSysProperties();
        int sessionExpireTimeSeconds1 = ejSysProperties2.getSessionExpireTimeSeconds();
        SecuritySession securitySession1 = sessionStrategy.refreshSession(token, sessionExpireTimeSeconds1, TimeUnit.SECONDS);
        return EasyResult.ok(securitySession1);
    }

}
