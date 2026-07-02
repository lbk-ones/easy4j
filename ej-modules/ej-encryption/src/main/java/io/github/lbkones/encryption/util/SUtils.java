package io.github.lbkones.encryption.util;

import io.github.lbkones.encryption.config.EncryptionProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * Spring 工具类
 * 提供获取 Spring Bean、获取环境配置参数、获取请求信息等功能
 */
public class SUtils implements ApplicationContextAware {


    private static ApplicationContext context;
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SUtils.context = applicationContext;
    }

    public static EncryptionProperties getEncryptProperties(){
        try{
            Environment environment = context.getEnvironment();
            Binder binder = Binder.get(environment);
            BindResult<EncryptionProperties> easy4j = binder.bind("easy4j.encryption", EncryptionProperties.class);
            return easy4j.get();
        }catch (Exception e){
            return new EncryptionProperties();
        }
    }


    /**
     * ========================= Bean 获取相关 =========================
     */

    /**
     * 根据 Bean 名称获取 Bean
     *
     * @param beanName Bean 名称
     * @return Bean 实例
     */
    public static Object getBean(String beanName) {
        return context == null ? null : context.getBean(beanName);
    }

    /**
     * 根据 Bean 类型获取 Bean
     *
     * @param clazz Bean 类型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return context == null ? null : context.getBean(clazz);
    }

    /**
     * 根据 Bean 名称和类型获取 Bean
     *
     * @param beanName Bean 名称
     * @param clazz    Bean 类型
     * @return Bean 实例
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return context == null ? null : context.getBean(beanName, clazz);
    }

    /**
     * ========================= 环境参数获取相关 =========================
     */

    /**
     * 从 Spring 环境中获取配置参数
     * 对应 application.properties 或 application.yml 中的配置
     *
     * @param key 参数名（支持点号分隔，如 "spring.datasource.url"）
     * @return 参数值，如果不存在返回 null
     */
    public static String getProperty(String key) {
        Environment environment = context.getEnvironment();
        return environment.getProperty(key);
    }

    /**
     * 从 Spring 环境中获取配置参数，带默认值
     *
     * @param key          参数名
     * @param defaultValue 默认值
     * @return 参数值，如果不存在返回默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return context.getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 从 Spring 环境中获取配置参数并转换为指定类型
     * 支持 String、Integer、Long、Boolean、Double 等类型
     *
     * @param key  参数名
     * @param type 目标类型
     * @return 转换后的参数值
     */
    public static <T> T getProperty(String key, Class<T> type) {
        return context.getEnvironment().getProperty(key, type);
    }

    /**
     * 从 Spring 环境中获取配置参数并转换为指定类型，带默认值
     *
     * @param key          参数名
     * @param type         目标类型
     * @param defaultValue 默认值
     * @return 转换后的参数值
     */
    public static <T> T getProperty(String key, Class<T> type, T defaultValue) {
        return context.getEnvironment().getProperty(key, type, defaultValue);
    }

    /**
     * 获取 Integer 类型的配置参数
     *
     * @param key          参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Integer getIntProperty(String key, Integer defaultValue) {
        return getProperty(key, Integer.class, defaultValue);
    }

    /**
     * 获取 Long 类型的配置参数
     *
     * @param key          参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Long getLongProperty(String key, Long defaultValue) {
        return getProperty(key, Long.class, defaultValue);
    }

    /**
     * 获取 Boolean 类型的配置参数
     *
     * @param key          参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Boolean getBooleanProperty(String key, Boolean defaultValue) {
        return getProperty(key, Boolean.class, defaultValue);
    }

    /**
     * ========================= HTTP 请求相关 =========================
     */

    /**
     * 获取 HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 获取 HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getResponse();
    }

    /**
     * 获取 HttpSession
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getSession();
    }

    /**
     * 获取请求参数
     *
     * @param paramName 参数名
     * @return 参数值
     */
    public static String getParameter(String paramName) {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getParameter(paramName);
    }

    /**
     * 获取请求参数数组
     *
     * @param paramName 参数名
     * @return 参数值数组
     */
    public static String[] getParameterValues(String paramName) {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getParameterValues(paramName);
    }

    /**
     * 获取所有请求参数
     *
     * @return 参数 Map
     */
    public static Map<String, String[]> getParameterMap() {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getParameterMap();
    }

    /**
     * 获取请求头
     *
     * @param headerName 请求头名
     * @return 请求头值
     */
    public static String getHeader(String headerName) {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getHeader(headerName);
    }

    /**
     * 获取 Cookie 值
     *
     * @param cookieName Cookie 名称
     * @return Cookie 值
     */
    public static String getCookieValue(String cookieName) {
        HttpServletRequest request = getRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 获取客户端 IP 地址
     * 支持代理环境下获取真实 IP
     *
     * @return 客户端 IP
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个 IP 的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 获取请求 URL（不包含查询字符串）
     *
     * @return 请求 URL
     */
    public static String getRequestUrl() {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getRequestURL().toString();
    }

    /**
     * 获取请求 URI（不包含协议和域名）
     *
     * @return 请求 URI
     */
    public static String getRequestUri() {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getRequestURI();
    }

    /**
     * 获取请求方法
     *
     * @return 请求方法（GET、POST 等）
     */
    public static String getRequestMethod() {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getMethod();
    }

    /**
     * ========================= Session 相关 =========================
     */

    /**
     * 获取 Session 属性
     *
     * @param attrName 属性名
     * @return 属性值
     */
    public static Object getSessionAttribute(String attrName) {
        HttpSession session = getSession();
        return session == null ? null : session.getAttribute(attrName);
    }

    /**
     * 设置 Session 属性
     *
     * @param attrName 属性名
     * @param value    属性值
     */
    public static void setSessionAttribute(String attrName, Object value) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(attrName, value);
        }
    }

    /**
     * 删除 Session 属性
     *
     * @param attrName 属性名
     */
    public static void removeSessionAttribute(String attrName) {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(attrName);
        }
    }
}
