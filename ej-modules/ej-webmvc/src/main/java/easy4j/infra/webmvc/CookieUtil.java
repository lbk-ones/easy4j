package easy4j.infra.webmvc;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
/**
 * Cookie 工具类：设置、获取、删除、支持HttpOnly/SameSite/Secure
 */
public class CookieUtil {

    /**
     * 设置Cookie
     * @param response 响应
     * @param name cookie名
     * @param value cookie值
     * @param maxAge 过期时间 秒；-1=浏览器关闭失效，0=删除
     * @param path 路径 默认 /
     * @param domain 域名
     * @param httpOnly 是否仅http不可js读取
     * @param secure 是否仅https传输
     * @param sameSite SameSite 值：Lax/Strict/None
     */
    public static void setCookie(HttpServletResponse response,
                                 String name,
                                 String value,
                                 int maxAge,
                                 String path,
                                 String domain,
                                 boolean httpOnly,
                                 boolean secure,
                                 String sameSite) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
        // 路径
        if (StringUtils.hasText(path)) {
            sb.append("; Path=").append(path);
        } else {
            sb.append("; Path=/");
        }
        // 过期时间
        sb.append("; Max-Age=").append(maxAge);
        // 域名
        if (StringUtils.hasText(domain)) {
            sb.append("; Domain=").append(domain);
        }
        // HttpOnly
        if (httpOnly) {
            sb.append("; HttpOnly");
        }
        // Secure
        if (secure) {
            sb.append("; Secure");
        }
        // SameSite
        if (StringUtils.hasText(sameSite)) {
            sb.append("; SameSite=").append(sameSite);
        }
        response.addHeader("Set-Cookie", sb.toString());
    }

    /**
     * 重载：常用默认配置
     * SameSite=Lax，path=/，无域名
     * SameSite=Lax:
     * 跨站 GET 跳转、链接点击、首页跳转 → 允许带 Cookie
     * 跨站 POST / 接口请求 / 表单提交 / AJAX → 禁止带 Cookie
     */
    public static void setCookie(HttpServletResponse response,
                                 String name,
                                 String value,
                                 int maxAge,
                                 boolean httpOnly,
                                 boolean secure) {
        setCookie(response, name, value, maxAge, "/", null, httpOnly, secure, "Lax");
    }

    /**
     * 获取Cookie值
     */
    public static String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 删除Cookie
     */
    public static void removeCookie(HttpServletResponse response, String name) {
        // Max-Age=0 立即失效
        setCookie(response, name, "", 0, "/", null, true, false, "Lax");
    }

    /**
     * 删除指定域名路径的Cookie
     */
    public static void removeCookie(HttpServletResponse response, String name, String path, String domain) {
        setCookie(response, name, "", 0, path, domain, true, false, "Lax");
    }


    // ========== 业务常用快捷方法 ==========

    /**
     * 设置登录Token Cookie（HttpOnly 推荐）
     * @param maxAge 秒
     */
    public static void setTokenCookie(HttpServletResponse response, String cookieName, String token, int maxAge) {
        // 本地开发secure=false；生产HTTPS改为true
        setCookie(response, cookieName, token, maxAge, true, false);
    }

    /**
     * 清除登录Token Cookie
     */
    public static void clearTokenCookie(HttpServletResponse response, String cookieName) {
        removeCookie(response, cookieName);
    }
}