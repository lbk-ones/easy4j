package easy4j.module.sauth.authentication;

/**
 * 作用域
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public enum AuthenticationScopeType {

    /**
     * 拦截器
     */
    Interceptor,
    /**
     * 认证
     */
    Authentication,
    /**
     * 认证和拦截器
     */
    AuthenticationAndInterceptor,

}
