package easy4j.module.sauth.core.loaduser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.condition.FWhereBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.encryption.IPwdEncryptionService;
import org.springframework.beans.factory.InitializingBean;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 从数据库中获取用户信息，默认实现（如果默认实现但是默认没有建表则报错，提示设置EASY4J_SIMPLE_AUTH_USER_IMPL_TYPE=default）
 * 有几种情况：
 * 1、当前服务没有使用系统默认的用户信息表
 * 2、当前服务使用的是业务系统自己的用户信息表
 *
 * @author bokun.li
 * @date 2025-07-26
 */
public class LoadUserByDbDefault implements LoadUserByDb, InitializingBean {

    ISecurityEasy4jUser simpleUser;


    @Resource
    DBAccess dbAccess;


    @Resource
    IPwdEncryptionService encryptionService;


    @Override
    public void afterPropertiesSet() throws Exception {
        simpleUser = LoadUserApi.getSimpleUser();
    }

    @Override
    public boolean select() {

        // 如果配置了查询外部用户信息的bean那么就说明要走外部逻辑,不走内部默认逻辑（内部默认逻辑需要建表）
        boolean authEnable = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_ENABLE, boolean.class);
        // 用户信息的实现必须是 default才能走这个类进行查询
        String implType = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_USER_IMPL_TYPE);
        // 只有服务端才能使用这个类查询
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_IS_SERVER, boolean.class);
        boolean equals1 = StrUtil.equals(SP.DEFAULT, implType);
        if (!equals1) {
            return false;
            //throw new IllegalArgumentException(SysLog.compact("the 【" + SysConstant.EASY4J_SIMPLE_AUTH_USER_IMPL_TYPE + "】 should be " + SP.DEFAULT));
        }
        return authEnable && isServer;
    }

    @Override
    public ISecurityEasy4jUser loadUserByUserName(String username) {

        if (null != simpleUser) {
            if (StrUtil.equals(simpleUser.getUsername(), username)) {
                return simpleUser;
            }
        }

        WhereBuild equal = FWhereBuild.get(SecurityUser.class).equal(SecurityUser::getUsername, username);
        List<SecurityUser> securityUsers = dbAccess.selectByCondition(equal, SecurityUser.class);
        if (CollUtil.isNotEmpty(securityUsers)) {
            return securityUsers.get(0);
        }
        return null;
    }

    @Override
    public ISecurityEasy4jUser loadUserByUserId(long userId) {
        if (null != simpleUser) {
            if (StrUtil.equals(simpleUser.getUsername(), String.valueOf(userId))) {
                return simpleUser;
            }
        }
        return null;
    }
}
