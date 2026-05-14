package io.github.lbkones.config.httpnacos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
/**
 * ==================== 策略实现2: v3.x 定时轮询策略 ====================
 */
/**
 * 定时轮询监听策略（v3.x）
 * 原理：定期轮询获取配置，与本地缓存对比，如有变更则触发回调
 */
public class PollingListenerStrategy implements ConfigListenerStrategy {

    private static final int POLLING_INTERVAL = 10000; // 10秒轮询一次
    private static final String CHARSET = "UTF-8";

    private String serverAddr;
    private String namespace;
    private Map<String, ConfigCache> configCacheMap;
    private Map<String, PollingTask> pollingTasks;
    private ExecutorService executorService;
    /**
     * 轮询任务
     */
    private static class PollingTask {
        volatile boolean running = true;
        Future<?> future;
    }

    public PollingListenerStrategy(String serverAddr, String namespace,
                                   Map<String, ConfigCache> configCacheMap,
                                   ExecutorService executorService) {
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.configCacheMap = configCacheMap;
        this.executorService = executorService;
        this.pollingTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void startListening(String dataId, String group, ConfigChangeCallback callback) throws Exception {
        String taskKey = dataId + "#" + group;

        if (pollingTasks.containsKey(taskKey)) {
            System.out.println("[Polling] Listening already started for " + taskKey);
            return;
        }

        PollingTask task = new PollingTask();
        task.future = executorService.submit(() -> {
            doPollingListen(dataId, group, callback, task);
        });

        pollingTasks.put(taskKey, task);
        System.out.println("[Polling] Started listening: " + taskKey);
    }

    private void doPollingListen(String dataId, String group,
                                 ConfigChangeCallback callback, PollingTask task) {
        while (task.running) {
            try {
                // 1. 获取远程配置
                String newContent = getRemoteConfig(dataId, group);

                // 2. 与缓存对比
                String cacheKey = dataId + "#" + group;
                ConfigCache cache = configCacheMap.get(cacheKey);

                if (cache == null || !cache.content.equals(newContent)) {
                    // 配置有变更
                    updateCache(dataId, group, newContent);
                    callback.onConfigChanged(dataId, group, newContent);
                    System.out.println("[Polling] Config changed detected for " + cacheKey);
                }

                if (!task.running) break;

                // 3. 等待轮询间隔
                Thread.sleep(POLLING_INTERVAL);

            } catch (InterruptedException e) {
                if (task.running) {
                    System.err.println("[Polling] Interrupted: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("[Polling] Error: " + e.getMessage());
                try {
                    Thread.sleep(2000); // 错误后延迟重试
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
    }

    private String getRemoteConfig(String dataId, String group) throws Exception {
        String url = String.format("http://%s/nacos/v1/cs/configs", serverAddr);
        url += "?dataId=" + URLEncoder.encode(dataId, CHARSET);
        url += "&group=" + URLEncoder.encode(group, CHARSET);
        if (!namespace.isEmpty()) {
            url += "&tenant=" + URLEncoder.encode(namespace, CHARSET);
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        return readResponse(conn);
    }

    private void updateCache(String dataId, String group, String content) {
        String cacheKey = dataId + "#" + group;
        configCacheMap.put(cacheKey, new ConfigCache(content));
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            return baos.toString(CHARSET);
        } catch (IOException e) {
            if (conn.getResponseCode() >= 400) {
                try (InputStream is = conn.getErrorStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, length);
                    }

                    return baos.toString(CHARSET);
                }
            }
            throw e;
        }
    }

    @Override
    public void stopListening(String dataId, String group) {
        String taskKey = dataId + "#" + group;
        PollingTask task = pollingTasks.get(taskKey);
        if (task != null) {
            task.running = false;
            task.future.cancel(true);
            pollingTasks.remove(taskKey);
            System.out.println("[Polling] Stopped listening: " + taskKey);
        }
    }

    @Override
    public String getStrategyName() {
        return "Polling (v3.x)";
    }
}
