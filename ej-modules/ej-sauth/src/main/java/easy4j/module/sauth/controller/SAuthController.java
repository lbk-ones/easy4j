package easy4j.module.sauth.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.func.LambdaUtil;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

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

}
