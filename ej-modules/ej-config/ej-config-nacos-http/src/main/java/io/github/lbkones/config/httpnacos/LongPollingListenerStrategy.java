package io.github.lbkones.config.httpnacos;

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
import java.util.concurrent.Future;

/**
 * 长轮询监听策略（v1.x 和 v2.x）
 * <br/>
 * 原理：定期发送配置MD5值，服务器如果有变更立即返回，否则等待30秒
 * <br/>
 * 支持用户名密码认证和AccessKey/SecretKey签名认证
 */
public class LongPollingListenerStrategy implements ConfigListenerStrategy {

    private static final int LONG_POLLING_TIMEOUT = 30000;
    private static final String CHARSET = "UTF-8";

    private final String serverAddr;
    private final String namespace;
    private final Map<String, ConfigCache> configCacheMap;
    private final Map<String, ListeningTask> listeningTasks;
    private final ExecutorService executorService;
    private final NacosAuthHelper authHelper;

    private static class ListeningTask {
        volatile boolean running = true;
        Future<?> future;
    }

    public LongPollingListenerStrategy(String serverAddr, String namespace,
                                       Map<String, ConfigCache> configCacheMap,
                                       ExecutorService executorService) {
        this(serverAddr, namespace, configCacheMap, executorService, null);
    }

    public LongPollingListenerStrategy(String serverAddr, String namespace,
                                       Map<String, ConfigCache> configCacheMap,
                                       ExecutorService executorService,
                                       NacosAuthHelper authHelper) {
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.configCacheMap = configCacheMap;
        this.executorService = executorService;
        this.authHelper = authHelper;
        this.listeningTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void startListening(String dataId, String group, ConfigChangeCallback callback) throws Exception {
        String taskKey = dataId + "#" + group;

        if (listeningTasks.containsKey(taskKey)) {
            System.out.println("[LongPolling] Listening already started for " + taskKey);
            return;
        }

        ListeningTask task = new ListeningTask();
        task.future = executorService.submit(() -> {
            doLongPollingListen(dataId, group, callback, task);
        });

        listeningTasks.put(taskKey, task);
        System.out.println("[LongPolling] Started listening: " + taskKey);
    }

    private void doLongPollingListen(String dataId, String group,
                                     ConfigChangeCallback callback, ListeningTask task) {
        while (task.running) {
            try {
                ensureAuthenticated();

                String listeningConfigs = buildListeningConfigs(dataId, group);
                String response = longPolling(listeningConfigs);

                if (response != null && !response.isEmpty()) {
                    String newContent = getRemoteConfig(dataId, group);
                    updateCache(dataId, group, newContent);
                    callback.onConfigChanged(dataId, group, newContent);
                }

                if (!task.running) break;
                Thread.sleep(100);

            } catch (InterruptedException e) {
                if (task.running) {
                    System.err.println("[LongPolling] Interrupted: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("[LongPolling] Error: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
    }

    /**
     * 执行长轮询请求
     */
    private String longPolling(String listeningConfigs) throws Exception {
        String url = String.format("http://%s/nacos/v1/cs/configs/listener", serverAddr);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setRequestProperty("Long-Pulling-Timeout", String.valueOf(LONG_POLLING_TIMEOUT));
        conn.setConnectTimeout(35000);
        conn.setReadTimeout(35000);
        conn.setDoOutput(true);

        if (authHelper != null) {
            authHelper.setAuthHeader(conn);
        }

        String body;
        if (authHelper != null && authHelper.getAccessToken() != null) {
            String signedConfigs = authHelper.signListeningConfig(listeningConfigs);
            body = "Listening-Configs=" + URLEncoder.encode(signedConfigs, CHARSET);
        } else {
            body = "Listening-Configs=" + URLEncoder.encode(listeningConfigs, CHARSET);
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(CHARSET));
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        String response = readResponse(conn);

        if (statusCode == 200 && response != null && !response.isEmpty()) {
            return response;
        }
        return null;
    }

    /**
     * 构建监听配置报文
     * 格式: dataId^2group^2contentMD5^2tenant^1
     */
    private String buildListeningConfigs(String dataId, String group) {
        String cacheKey = dataId + "#" + group;
        ConfigCache cache = configCacheMap.get(cacheKey);
        String md5 = cache != null ? cache.md5 : "";

        String separator2 = Character.toString((char) 2);
        String separator1 = Character.toString((char) 1);

        return dataId + separator2 + group + separator2 + md5 + separator2 + namespace + separator1;
    }

    private String getRemoteConfig(String dataId, String group) throws Exception {
        ensureAuthenticated();

        String url = String.format("http://%s/nacos/v1/cs/configs", serverAddr);
        url += "?dataId=" + URLEncoder.encode(dataId, CHARSET);
        url += "&group=" + URLEncoder.encode(group, CHARSET);
        if (!namespace.isEmpty()) {
            url += "&tenant=" + URLEncoder.encode(namespace, CHARSET);
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

    private void ensureAuthenticated() {
        if (authHelper != null && authHelper.needRefreshToken()) {
            authHelper.login();
        }
    }

    @Override
    public void stopListening(String dataId, String group) {
        String taskKey = dataId + "#" + group;
        ListeningTask task = listeningTasks.get(taskKey);
        if (task != null) {
            task.running = false;
            task.future.cancel(true);
            listeningTasks.remove(taskKey);
            System.out.println("[LongPolling] Stopped listening: " + taskKey);
        }
    }

    @Override
    public String getStrategyName() {
        return "LongPolling (v1.x/v2.x)";
    }
}
