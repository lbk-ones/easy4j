package easy4j.module.sauth.core;

import easy4j.module.base.exception.EasyException;
import easy4j.module.sauth.domain.SecurityUserInfo;

public interface SecurityAuthentication {

    /**
     * 权限认证
     *
     * @param user
     * @return
     */
    boolean verifyAuthentication(SecurityUserInfo user) throws EasyException;

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
