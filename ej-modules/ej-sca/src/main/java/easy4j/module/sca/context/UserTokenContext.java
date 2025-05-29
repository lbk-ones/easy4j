package easy4j.module.sca.context;

/**
 * UserTokenContext
 *
 * @author bokun.li
 * @date 2025-05
 */
public class UserTokenContext {
    private static final ThreadLocal<String> userToken = new ThreadLocal<>();

    public UserTokenContext() {
    }

    public static String getToken() {
        return (String) userToken.get();
    }

    public static void setToken(String token) {
        userToken.set(token);
    }

    public static void remove() {
        userToken.remove();
    }
}