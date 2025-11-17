package easy4j.module.sauth.authentication;

/**
 * 认证器方式
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public enum AuthenticationType {

    /**
     * shaToken 认证 这个认证方式一般用来拦截
     * @see ShaTokenAuthentication
     */
    ShaToken,

    /**
     * 基于用户名和密码
     * @see UserNamePasswordAuthentication
     */
    UserNamePassword,

    /**
     * 其他方式认证
     * @see OtherAuthentication
     */
    Other,

    /**
     * 基于 BasicAuth
     * @see BasicAuthAuthentication
     */
    Basic,

    /**
     * 基于 BearerToken
     * @see BearerTokenAuthentication
     */
    BearerToken,

    /**
     * 以AccessToken来访问系统
     * @see AccessTokenAuthentication
     */
    AccessToken,

    /**
     * 基于 BearerToken 和 JwtToken
     * @see BearerJwtTokenAuthAuthentication
     */
    BearerJwtToken,

    /**
     * 基于JWTToken
     * @see JwtAuthAuthentication
     */
    Jwt,

    /**
     * 简单认证 配置中固定用户名和密码
     * @see SimpleUserAuthentication
     */
    Simple,

}
