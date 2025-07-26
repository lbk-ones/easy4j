package easy4j.module.sauth.core.loaduser;

/**
 * 只从数据库中查询用户信息
 *
 * @author bokun.li
 * @date 2025-07-26
 */
public interface LoadUserByDb extends LoadUserBy {

    @Override
    default boolean select() {
        return true;
    }
}
