package io.github.lbkones.config.httpnacos;


/**
 * ==================== 核心客户端：统一入口 ====================
 */

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
    private final ExecutorService executorService;
    private final Map<String, ConfigCache> configCacheMap;
    private final Map<String, ConfigListener> listenerMap;
    private final ReentrantReadWriteLock lock;
    private volatile boolean shutdown = false;
    
    public interface ConfigListener {
        void onConfigChange(String dataId, String group, String newContent);
    }
    
    public CompatibleNacosHttpClient(String serverAddr, String namespace) throws Exception {
        this.serverAddr = serverAddr;
        this.namespace = namespace == null ? "" : namespace;
        this.executorService = Executors.newCachedThreadPool();
        this.configCacheMap = new ConcurrentHashMap<>();
        this.listenerMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        
        // 1. 检测版本
        detectAndInitStrategy();
    }
    
    private void detectAndInitStrategy() throws Exception {
        System.out.println("========== Detecting Nacos Version ==========");
        
        NacosVersionDetector detector = new NacosVersionDetector(serverAddr);
        this.detectedVersion = detector.detect();
        
        System.out.println("Detected Version: " + detectedVersion.getVersionName());
        
        // 2. 根据版本选择策略
        switch (detectedVersion) {
            case V1_X:
            case V2_X:
                this.strategy = new LongPollingListenerStrategy(
                    serverAddr, namespace, adaptCacheMap(), executorService);
                System.out.println("Using Strategy: " + strategy.getStrategyName());
                break;
                
            case V3_X:
                this.strategy = new PollingListenerStrategy(
                    serverAddr, namespace, adaptCacheMap(), executorService);
                System.out.println("Using Strategy: " + strategy.getStrategyName());
                break;
                
            case UNKNOWN:
            default:
                System.out.println("Warning: Unknown version, fallback to Polling strategy");
                this.strategy = new PollingListenerStrategy(
                    serverAddr, namespace, adaptCacheMap(), executorService);
                break;
        }
        
        System.out.println("==========================================\n");
    }
    
    /**
     * 将String缓存转换为ConfigCache缓存供策略使用
     */
    private Map<String, ConfigCache> adaptCacheMap() {
        return configCacheMap;
    }
    
    /**
     * 获取配置
     */
    public String getConfig(String dataId, String group) throws Exception {
        String url = String.format("http://%s/nacos/v1/cs/configs", serverAddr);
        url += "?dataId=" + URLEncoder.encode(dataId, StandardCharsets.UTF_8);
        url += "&group=" + URLEncoder.encode(group, StandardCharsets.UTF_8);
        if (!namespace.isEmpty()) {
            url += "&tenant=" + URLEncoder.encode(namespace, StandardCharsets.UTF_8);
        }
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        String response = readResponse(conn);
        
        // 缓存
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
        String url = String.format("http://%s/nacos/v1/cs/configs", serverAddr);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        
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
        String url = String.format("http://%s/nacos/v1/cs/configs", serverAddr);
        url += "?dataId=" + URLEncoder.encode(dataId, StandardCharsets.UTF_8);
        url += "&group=" + URLEncoder.encode(group, StandardCharsets.UTF_8);
        if (!namespace.isEmpty()) {
            url += "&tenant=" + URLEncoder.encode(namespace, StandardCharsets.UTF_8);
        }
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        String response = readResponse(conn);
        
        // 清除缓存
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
        
        // 先获取初始值
        try {
            getConfig(dataId, group);
        } catch (Exception e) {
            System.err.println("Failed to get initial config: " + e.getMessage());
        }
        
        // 启动监听
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
    
    // ===================== 使用示例 =====================
    public static void main(String[] args) throws Exception {
        // 创建客户端（自动检测版本）
        CompatibleNacosHttpClient client = new CompatibleNacosHttpClient("127.0.0.1:8848", "develop");
        
        System.out.println("Detected Version: " + client.getDetectedVersion().getVersionName());
        System.out.println("Using Strategy: " + client.getStrategy().getStrategyName());
        System.out.println();
        
        // 1. 获取配置
        System.out.println("=== 获取配置 ===");
        try {
            String config = client.getConfig("common.properties", "dataspace-service");
            System.out.println("Current Config: " + config);
        } catch (Exception e) {
            System.err.println("Failed to get config: " + e.getMessage());
        }
        
        // 2. 发布配置
        System.out.println("\n=== 发布配置 ===");
        try {
            boolean success = client.publishConfig("myapp.properties", "DEFAULT_GROUP",
                "key1=value1\nkey2=value2\nkey3=value3");
            System.out.println("Publish success: " + success);
        } catch (Exception e) {
            System.err.println("Failed to publish config: " + e.getMessage());
        }
        
        // 3. 监听配置变更
        System.out.println("\n=== 监听配置变更 ===");
        try {
            client.addListener("common.properties", "dataspace-service",
                (dataId, group, newContent) -> {
                    System.out.println("\n[Config Changed] " + dataId + "/" + group);
                    System.out.println("New Content:\n" + newContent);
                });
            System.out.println("Listening started...");
        } catch (Exception e) {
            System.err.println("Failed to add listener: " + e.getMessage());
        }
        
        // 保持运行
        System.out.println("\nPress Ctrl+C to exit...");
        Thread.sleep(Long.MAX_VALUE);
    }
}