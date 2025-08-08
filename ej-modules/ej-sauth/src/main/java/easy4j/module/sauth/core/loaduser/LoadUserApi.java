package easy4j.module.sauth.core.loaduser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.SysConstant;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.encryption.IPwdEncryptionService;

import java.util.Date;

/**
 * 查询用户信息的入口
 *
 * @author bokun.li
 * @date 2025-07-26
 */
public final class LoadUserApi {

    private static LoadUserByDb loadUserByDb;
    private static LoadUserByRpc loadUserByRpc;


    private static LoadUserByDb getLoadUserByDb() {
        if (loadUserByDb == null) {
            loadUserByDb = SpringUtil.getBean(LoadUserByDb.class);
        }
        if (!loadUserByDb.select()) {
            //Easy4j.error("not select load user rule ! please check");
            return null;
        }
        return loadUserByDb;
    }

    private static LoadUserByRpc getLoadUserByRpc() {
        if (loadUserByRpc == null) {
            loadUserByRpc = SpringUtil.getBean(LoadUserByRpc.class);
        }
        if (!loadUserByRpc.select()) {
            Easy4j.error("not select load user rpc rule ! please check");
            return null;
        }
        return loadUserByRpc;
    }

    // 是否应该调用RPC去查询
    // 如果手动配置了直接查询数据库那就查询数据库（easy4j.direct-query-user-db=true）
    // 如果表示了是服务端那么就查询数据库
    // 如果没有手动配置直接查数据库
    // 返回true代表查询数据库
    private static LoadUserBy loadUserBy() {
        LoadUserBy loadUserByDb1 = getLoadUserByDb();
        if (loadUserByDb1 != null) return loadUserByDb1;
        LoadUserByRpc loadUserByRpc1 = getLoadUserByRpc();
        if (null == loadUserByRpc1) {
            throw new EasyException("not determine user load rule please check!");
        }
        return loadUserByRpc1;
    }

    public static ISecurityEasy4jUser getByUserName(String userName) {
        LoadUserBy loadUserBy = loadUserBy();

        return loadUserBy.loadUserByUserName(userName);
    }

    public static ISecurityEasy4jUser getUserByUserId(long userId) {
        return null;
    }


    public static ISecurityEasy4jUser getSimpleUser() {
        String username = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_USERNAME);
        String password = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_PASSWORD);
        if (StrUtil.isAllNotBlank(username, password)) {
            String username_CN = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_USERNAME_CN);
            SecurityUser securityUserInfo = new SecurityUser();
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
            IPwdEncryptionService iPwdEncryptionService = SpringUtil.getBean(IPwdEncryptionService.class);
            String encrypt = iPwdEncryptionService.encrypt(password, securityUserInfo);
            securityUserInfo.setPassword(encrypt);
            return securityUserInfo;
        }
        return null;
    }
}
