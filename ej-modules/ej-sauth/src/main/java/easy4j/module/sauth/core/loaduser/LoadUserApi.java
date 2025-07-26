package easy4j.module.sauth.core.loaduser;

import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;

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
            Easy4j.error("not select load user rule ! please check");
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

}
