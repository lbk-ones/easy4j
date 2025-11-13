package easy4j.module.sauth.authentication;

/**
 * 认证器方式
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public enum AuthenticationType {

    /**
     * shaToken 认证
     */
    ShaToken,

    /**
     * 基于用户名和密码
     */
    UserNamePassword,

    /**
     * 其他方式认证
     */
    Other,

    /**
     * 基于 BasicAuth
     */
    Basic,

    /**
     * 基于JWTToken
     */
    Jwt,

    /**
     * 简单认证 配置中固定用户名和密码
     */
    Simple,

}
