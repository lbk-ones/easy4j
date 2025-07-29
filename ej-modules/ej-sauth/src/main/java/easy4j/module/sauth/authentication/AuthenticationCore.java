package easy4j.module.sauth.authentication;

import easy4j.module.sauth.core.Easy4jSecurityService;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;

import java.util.function.Consumer;

/**
 * 认证器的认证流程
 * @see Easy4jSecurityService#authentication(ISecurityEasy4jUser, Consumer)
 * @author bokun.li
 * @date 2025-07-27
 */
public interface AuthenticationCore {

    /**
     * Authentication Type
     * 返回认证类型（相同的会覆盖）
     *
     * @author bokun.li
     * @date 2025-07-27
     */
    String getName();

    /**
     * 查询用户信息
     *
     * @param context
     * @return
     */
    ISecurityEasy4jUser queryUser(AuthenticationContext context);

    /**
     * 查询会话信息
     *
     * @param context
     * @return
     */
    ISecurityEasy4jSession querySession(AuthenticationContext context);


    /**
     * 预校验认证信息
     *
     * @param context
     */
    void verifyPre(AuthenticationContext context);

    /**
     * 认证
     *
     * @param context
     */
    void verify(AuthenticationContext context);

    /**
     * 检查当前用户是否已经失效 (黑白名单，是否过期，是否锁定，用户是否存在) 等等
     *
     * @param context
     * @return
     */
    boolean checkUser(AuthenticationContext context);

    /**
     * 刷新会话
     *
     * @param context
     */
    void refreshSession(AuthenticationContext context);

    /**
     * 在倒数第二步选择将会话信息绑定到web上下文给后续业务使用
     *
     * @param context
     */
    void bindSessionToCtx(AuthenticationContext context);

    /**
     * 生成用户信息
     *
     * @param context
     */
    OnlineUserInfo genOnlineUserInfo(AuthenticationContext context);

}
