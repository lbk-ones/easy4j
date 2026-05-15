package io.github.lbkones.config.httpnacos;

/**
 * ==================== 核心客户端：统一入口 ====================
 */

import easy4j.infra.common.utils.SysLog;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 版本兼容的Nacos HTTP客户端
 * <br/>
 * 支持Nacos 2.x/3.x版本自动检测和认证功能
 * <br/>
 * 认证方式：
 * 1. 用户名密码认证：通过login接口获取token
 * 2. AccessKey/SecretKey签名认证
 */
public class CompatibleNacosHttpClient {

    private final String serverAddr;
    private final String namespace;
    /**
     * -- GETTER --
     *  获取使用的策略
     */
    @Getter
    private ConfigListenerStrategy strategy;
    /**
     * -- GETTER --
     *  获取检测到的版本
     */
    @Getter
    private NacosVersionDetector.NacosVersion detectedVersion;

    private NacosAuthHelper authHelper;

    private final ExecutorService executorService;
    private final Map<String, ConfigCache> configCacheMap;
    private final Map<String, ConfigListener> listenerMap;
    private final ReentrantReadWriteLock lock;
    private volatile boolean shutdown = false;

    public interface ConfigListener {
        void onConfigChange(String dataId, String group, String newContent);
    }

    /**
     * 构造函数（无认证）
     */
    public CompatibleNacosHttpClient(String serverAddr, String namespace) throws Exception {
        this(serverAddr, namespace, null, null);
    }

    /**
     * 构造函数（用户名密码认证）
     */
    public CompatibleNacosHttpClient(String serverAddr, String namespace, String username, String password) throws Exception {
        this.serverAddr = serverAddr;
        this.namespace = namespace == null ? "" : namespace;
        this.executorService = Executors.newCachedThreadPool();
        this.configCacheMap = new ConcurrentHashMap<>();
        this.listenerMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();

        if (username != null && password != null) {
            this.authHelper = new NacosAuthHelper(serverAddr, namespace, username, password);
        }

        detectAndInitStrategy();
    }

    /**
     * 构造函数（AccessKey/SecretKey认证）
     */
    public CompatibleNacosHttpClient(String serverAddr, String namespace, String accessKey, String secretKey, String signType) throws Exception {
        this.serverAddr = serverAddr;
        this.namespace = namespace == null ? "" : namespace;
        this.executorService = Executors.newCachedThreadPool();
        this.configCacheMap = new ConcurrentHashMap<>();
        this.listenerMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();

        if (accessKey != null && secretKey != null) {
            this.authHelper = new NacosAuthHelper(serverAddr, namespace, accessKey, secretKey, signType);
        }

        detectAndInitStrategy();
    }

    private void detectAndInitStrategy() throws Exception {
        System.out.println(SysLog.compact("========== Detecting Nacos Version =========="));

        NacosVersionDetector detector = new NacosVersionDetector(serverAddr);
        this.detectedVersion = detector.detect();

        System.out.println(SysLog.compact("Detected Version: " + detectedVersion.getVersionName()));

        switch (detectedVersion) {
            case V1_X:
            case V2_X:
                this.strategy = new LongPollingListenerStrategy(
                    serverAddr, namespace, adaptCacheMap(), executorService, authHelper);
                System.out.println(SysLog.compact("Using Strategy: " + strategy.getStrategyName()));
                break;

            case V3_X:
                this.strategy = new PollingListenerStrategy(
                    serverAddr, namespace, adaptCacheMap(), executorService, authHelper);
                System.out.println(SysLog.compact("Using Strategy: " + strategy.getStrategyName()));
                break;

            case UNKNOWN:
            default:
                System.out.println(SysLog.compact("Warning: Unknown version, fallback to Polling strategy"));
                this.strategy = new PollingListenerStrategy(
                    serverAddr, namespace, adaptCacheMap(), executorService, authHelper);
                break;
        }

        System.out.println(SysLog.compact("==========================================\n"));
    }

    private Map<String, ConfigCache> adaptCacheMap() {
        return configCacheMap;
    }

    /**
     * 获取配置
     */
    public String getConfig(String dataId, String group) throws Exception {
        ensureAuthenticated();

        String url = String.format("http://%s/nacos/v1/cs/configs", serverAddr);
        url += "?dataId=" + URLEncoder.encode(dataId, StandardCharsets.UTF_8);
        url += "&group=" + URLEncoder.encode(group, StandardCharsets.UTF_8);
        if (!namespace.isEmpty()) {
            url += "&tenant=" + URLEncoder.encode(namespace, StandardCharsets.UTF_8);
        }

        if (authHelper != null) {
            url = authHelper.addAuthParams(url);
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (authHelper != null) {
            authHelper.setAuthHeader(conn);
        }

        String response = readResponse(conn);

        String cacheKey = dataId + "#" + group;
        lock.writeLock().lock();
        try {
            configCacheMap.put(cacheKey, new ConfigCache(response));
        } finally {
            lock.writeLock().unlock();
        }

        return response;
    }

    /**
     * 发布配置
     */
    public boolean publishConfig(String dataId, String group, String content) throws Exception {
        ensureAuthenticated();

        String url = String.format("http://%s/nacos/v1/cs/configs", serverAddr);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);

        if (authHelper != null) {
            authHelper.setAuthHeader(conn);
        }

        String body = "dataId=" + URLEncoder.encode(dataId, StandardCharsets.UTF_8) +
                     "&group=" + URLEncoder.encode(group, StandardCharsets.UTF_8) +
                     "&content=" + URLEncoder.encode(content, StandardCharsets.UTF_8);

        if (!namespace.isEmpty()) {
            body += "&tenant=" + URLEncoder.encode(namespace, StandardCharsets.UTF_8);
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        String response = readResponse(conn);
        return "true".equalsIgnoreCase(response.trim());
    }

    /**
     * 删除配置
     */
    public boolean removeConfig(String dataId, String group) throws Exception {
        ensureAuthenticated();

        String url = String.format("http://%s/nacos/v1/cs/configs", serverAddr);
        url += "?dataId=" + URLEncoder.encode(dataId, StandardCharsets.UTF_8);
        url += "&group=" + URLEncoder.encode(group, StandardCharsets.UTF_8);
        if (!namespace.isEmpty()) {
            url += "&tenant=" + URLEncoder.encode(namespace, StandardCharsets.UTF_8);
        }

        if (authHelper != null) {
            url = authHelper.addAuthParams(url);
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (authHelper != null) {
            authHelper.setAuthHeader(conn);
        }

        String response = readResponse(conn);

        String cacheKey = dataId + "#" + group;
        lock.writeLock().lock();
        try {
            configCacheMap.remove(cacheKey);
        } finally {
            lock.writeLock().unlock();
        }

        return "true".equalsIgnoreCase(response.trim());
    }

    /**
     * 添加配置监听
     */
    public void addListener(String dataId, String group, ConfigListener listener) throws Exception {
        String cacheKey = dataId + "#" + group;
        listenerMap.put(cacheKey, listener);

        try {
            getConfig(dataId, group);
        } catch (Exception e) {
            System.err.println("Failed to get initial config: " + e.getMessage());
        }

        strategy.startListening(dataId, group, listener::onConfigChange);
    }

    /**
     * 移除配置监听
     */
    public void removeListener(String dataId, String group) {
        String cacheKey = dataId + "#" + group;
        listenerMap.remove(cacheKey);
        strategy.stopListening(dataId, group);
    }

    /**
     * 确保已认证
     */
    private void ensureAuthenticated() {
        if (authHelper != null && authHelper.needRefreshToken()) {
            authHelper.login();
        }
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            if (conn.getResponseCode() >= 400) {
                try (InputStream is = conn.getErrorStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, length);
                    }

                    return baos.toString(StandardCharsets.UTF_8);
                }
            }
            throw e;
        }
    }

    public void shutdown() {
        shutdown = true;
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    /**
     * 获取认证助手
     */
    public NacosAuthHelper getAuthHelper() {
        return authHelper;
    }

}
