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
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.func.LambdaUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
        Dict dict = Dict.create()
                .set(LambdaUtil.getFieldName(SecuritySession::getShaToken), token);
        return EasyResult.ok(dbAccess.selectOneByMap(dict, SecuritySession.class));
    }

    @PostMapping("saveSession")
    public EasyResult<Object> saveSession(@RequestBody SecuritySession securitySession) {
        int i = dbAccess.saveOne(securitySession, SecuritySession.class);
        if (i > 0) {
            return EasyResult.ok(securitySession);
        } else {
            return EasyResult.ok(null);
        }
    }

    @DeleteMapping("deleteSession/{token}")
    public EasyResult<Object> deleteSession(@PathVariable(name = "token") String token) {
        Dict dict = Dict.create().set(LambdaUtil.getFieldName(SecuritySession::getShaToken), token);
        int i = dbAccess.deleteByMap(dict, SecuritySession.class);
        return EasyResult.ok(i);
    }

    @GetMapping("getSessionByUserName/{username}")
    public EasyResult<SecuritySession> getSessionByUserName(@PathVariable(name = "username") String username) {
        Dict dict = Dict.create()
                .set(LambdaUtil.getFieldName(SecuritySession::getUserName), username);
        SecuritySession securitySession = dbAccess.selectOneByMap(dict, SecuritySession.class);
        return EasyResult.ok(securitySession);
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
        Dict dict = Dict.create()
                .set(LambdaUtil.getFieldName(SecuritySession::getShaToken), token);
        SecuritySession securitySession = dbAccess.selectOneByMap(dict, SecuritySession.class);
        long expireTimeSeconds = securitySession.getExpireTimeSeconds();
        EjSysProperties ejSysProperties1 = Easy4j.getEjSysProperties();
        int sessionRefreshTimeRemaining = ejSysProperties1.getSessionRefreshTimeRemaining();
        if (expireTimeSeconds > 0 && (new Date().getTime() + sessionRefreshTimeRemaining * 1000L) >= expireTimeSeconds) {
            EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
            int sessionExpireTimeSeconds = ejSysProperties.getSessionExpireTimeSeconds();
            securitySession.setExpireTimeSeconds(new Date().getTime() + (sessionExpireTimeSeconds * 1000L));
            dbAccess.updateByPrimaryKey(securitySession, SecuritySession.class, false);
        }
        return EasyResult.ok(null);
    }

}
