package easy4j.module.sauth.core.loaduser;

/**
 * 只从远程RPC服务中查询用户信息
 *
 * @author bokun.li
 * @date 2025-07-26
 */
public interface LoadUserByRpc extends LoadUserBy {

    @Override
    default boolean select() {
        return true;
    }
}
