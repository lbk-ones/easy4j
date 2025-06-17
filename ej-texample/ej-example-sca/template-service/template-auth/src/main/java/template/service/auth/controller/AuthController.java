package template.service.auth.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.condition.FWhereBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.module.sauth.annotations.NoLogin;
import easy4j.module.sauth.core.Easy4jAuth;
import easy4j.module.sauth.core.EncryptionService;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.seed.CommonKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 鉴权
 * @author bokun.li
 * @date 2025/6/17
 */
@RestController
@RequestMapping("/auth-apply")
public class AuthController {

    @Autowired
    DBAccess dbAccess;

    @Autowired
    EncryptionService encryptionService;


    @NoLogin
    @PostMapping("/login/{username}/{password}")
    public EasyResult<Object> login(@PathVariable(name = "username") String username,
                                    @PathVariable(name = "password") String password){
        SecurityUser securityUser = new SecurityUser();
        securityUser.setUsername(username);
        securityUser.setPassword(password);
        WhereBuild equal = FWhereBuild.get(SecurityUser.class).equal(SecurityUser::getUsername, username);
        List<SecurityUser> securityUsers = dbAccess.selectByCondition(equal, SecurityUser.class);
        if(CollUtil.isEmpty(securityUsers)){
            securityUser.setUserId(CommonKey.gennerLong());
            String s = RandomUtil.randomString(4);
            securityUser.setPwdSalt(s);
            SecurityUserInfo securityUserInfo = securityUser.toSecurityUserInfo();
            String encrypt = encryptionService.encrypt(password, securityUserInfo);
            securityUser.setPassword(encrypt);
            int i = dbAccess.saveOne(securityUser, SecurityUser.class);
            Easy4j.info("用户写入"+i);
        }
        SecurityUserInfo securityUserInfo = securityUser.toSecurityUserInfo();
        // 不能使用加密之后的参数传进去
        securityUserInfo.setPassword(password);
        SecurityUserInfo login = Easy4jAuth.login(securityUserInfo, null);
        return EasyResult.ok(login);
    }



}