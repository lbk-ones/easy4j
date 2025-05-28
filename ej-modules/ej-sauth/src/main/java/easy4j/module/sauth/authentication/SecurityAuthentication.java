package easy4j.module.sauth.authentication;

import easy4j.module.base.exception.EasyException;
import easy4j.module.sauth.domain.SecurityUserInfo;

/**
 * 登录权限认证
 * token认证
 * 查询用户信息
 */
public interface SecurityAuthentication {

    /**
     * 权限认证
     *
     * @param user
     * @return
     */
    SecurityUserInfo verifyLoginAuthentication(SecurityUserInfo user) throws EasyException;

    /**
     * token授权 给登录之后拦截器使用
     *
     * @param token
     * @return
     * @throws EasyException
     */
    SecurityUserInfo tokenAuthentication(String token) throws EasyException;

    /**
     * 权限检查 (黑白名单，是否过期，是否锁定，用户是否存在)
     *
     * @return
     */
    boolean checkUser(SecurityUserInfo user) throws EasyException;


    /**
     * 查询用户信息
     *
     * @param username
     * @return
     */
    SecurityUserInfo getUserByUserName(String username);

}
