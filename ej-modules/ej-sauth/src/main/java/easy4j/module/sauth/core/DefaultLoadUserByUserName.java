package easy4j.module.sauth.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.condition.FWhereBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

public class DefaultLoadUserByUserName implements LoadUserByUserName, InitializingBean {

    SecurityUserInfo simpleUser;

    @Resource
    EncryptionService encryptionService;

    @Resource
    DBAccess dbAccess;

    @Override
    public void afterPropertiesSet() throws Exception {
        String username = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_USERNAME);
        String password = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_PASSWORD);
        if (StrUtil.isAllNotBlank(username, password)) {
            String username_CN = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_USERNAME_CN);
            SecurityUserInfo securityUserInfo = new SecurityUserInfo();
            securityUserInfo.setUsername(username);
            String salt = RandomUtil.randomString(4);
            securityUserInfo.setPwdSalt(salt);
            securityUserInfo.setCreateDate(new Date());
            securityUserInfo.setUsernameCn(username_CN);
            securityUserInfo.setAccountNonExpired(true);
            securityUserInfo.setAccountNonLocked(true);
            securityUserInfo.setCredentialsNonExpired(true);
            securityUserInfo.setEnabled(true);
            securityUserInfo.setExtMap(Maps.newHashMap());
            String encrypt = encryptionService.encrypt(password, securityUserInfo);
            securityUserInfo.setPassword(encrypt);
            simpleUser = securityUserInfo;
        }

    }

    @Override
    public SecurityUserInfo loadUserByUserName(String username) {
        if (null != simpleUser) {
            if (StrUtil.equals(simpleUser.getUsername(), username)) {
                return simpleUser;
            }
        }
        WhereBuild equal = FWhereBuild.get(SecurityUser.class).equal(SecurityUser::getUsername, username);
        List<SecurityUser> securityUsers = dbAccess.selectByCondition(equal, SecurityUser.class);
        if (CollUtil.isNotEmpty(securityUsers)) {
            SecurityUser securityUser = securityUsers.get(0);
            return securityUser.toSecurityUserInfo();
        }
        return null;
    }
}

