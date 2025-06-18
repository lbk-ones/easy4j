package easy4j.module.sauth.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.sca.Easy4jNacosInvokerApi;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.condition.FWhereBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.module.sauth.config.Config;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

public class DefaultLoadUserByUserName implements LoadUserByUserName, InitializingBean {
    public static final String LOAD_USER_BY_USER_NAME = "/sauth/loadUserByUserName";

    SecurityUserInfo simpleUser;

    @Resource
    EncryptionService encryptionService;

    @Resource
    DBAccess dbAccess;


    Easy4jNacosInvokerApi easy4jNacosInvokerApi;

    @Resource
    Easy4jContext easy4jContext;

    boolean isClient;

    String serverName;

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


        easy4jNacosInvokerApi = easy4jContext.get(Easy4jNacosInvokerApi.class);

        // boolean property = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_SERVER, boolean.class);
        if (!isServer) {
            serverName = Config.AUTH_SERVER_NAME;
            isClient = true;
        }

    }

    @Override
    public SecurityUserInfo loadUserByUserName(String username) {
        if (null != simpleUser) {
            if (StrUtil.equals(simpleUser.getUsername(), username)) {
                return simpleUser;
            }
        }
        if (isClient) {

            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(LOAD_USER_BY_USER_NAME + SP.SLASH + username)
                    .build();

            EasyResult<Object> securitySessionEasyResult = easy4jNacosInvokerApi.get(build);
            CheckUtils.checkRpcRes(securitySessionEasyResult);
            return CheckUtils.convertRpcRes(securitySessionEasyResult, SecurityUserInfo.class);
        } else {
            WhereBuild equal = FWhereBuild.get(SecurityUser.class).equal(SecurityUser::getUsername, username);
            List<SecurityUser> securityUsers = dbAccess.selectByCondition(equal, SecurityUser.class);
            if (CollUtil.isNotEmpty(securityUsers)) {
                SecurityUser securityUser = securityUsers.get(0);
                return securityUser.toSecurityUserInfo();
            }
        }

        return null;
    }
}

