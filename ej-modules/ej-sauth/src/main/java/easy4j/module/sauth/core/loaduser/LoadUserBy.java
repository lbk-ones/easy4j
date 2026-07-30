package easy4j.module.sauth.core.loaduser;

import easy4j.module.sauth.domain.ISecurityEasy4jUser;

/**
 * 加载用户信息
 * 2.1.4 删除 loadUserById这个方法 同时将loadUserByUserName传参改为
 * @author bokun.li
 * @since 1.0.0
 * @version 2.1.4
 */
public interface LoadUserBy {

    boolean select();


    /**
     * 用户信息肯定有 username 但是其他信息不敢保证 比如 authenticationType 就有可能没有
     * <hr/>
     * 如果有多种登录方式，且每种方式传入的username不一样那么这里需要判断 认证类型 也就是authenticationType 根据不同的认证类型，走不同的查询逻辑，同时留一种兜底查询
     * <hr/>
     * 如果就一种登录方式，那么这里可以不用判断认证类型,直接查就是了
     * @param req 大部分时候是认证的时候传入进来的用户请求信息
     */
    ISecurityEasy4jUser loadUserByUserName(ISecurityEasy4jUser req);


}
