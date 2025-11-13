package easy4j.module.sauth.authentication;

import easy4j.module.sauth.domain.ISecurityEasy4jUser;
/**
 * 其他鉴权方式
 * @author bokun.li
 * @date 2025/11/13
 */
public interface LoadAuthentication {

    /**
     * 查询用户信息
     * @param req 传入的用户信息
     * @return
     */
    ISecurityEasy4jUser getUserBy(ISecurityEasy4jUser req);

    /**
     * 鉴权
     * @param req 传入的用户信息
     * @return 返回true代表认证成功
     */
    boolean verify(ISecurityEasy4jUser req);

}
