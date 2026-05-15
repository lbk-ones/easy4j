package io.github.lbkones.config.httpnacos;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import easy4j.infra.common.utils.json.JacksonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Nacos HTTP 认证辅助类
 * <br/>
 * 支持Nacos 2.x/3.x的用户名密码认证和AccessKey/SecretKey签名认证
 * <br/>
 * Nacos认证机制：
 * 1. 用户名密码认证：通过/login接口获取accessToken，后续请求携带token
 * 2. 签名认证：通过AccessKey和SecretKey对请求参数进行HMAC-SHA1签名
 * <br/>
 *
 * @author libokun
 */
@Slf4j
public class NacosAuthHelper {

    private static final String LOGIN_PATH = "/nacos/v1/auth/login";
    private static final int DEFAULT_TIMEOUT = 5000;

    private final String serverAddr;
    private final String namespace;
    @Getter
    private String accessToken;
    @Getter
    private long tokenExpireTime;
    private volatile boolean isAuthenticated = false;

    private String username;
    private String password;
    private String accessKey;
    private String secretKey;
    private String signType;

    public NacosAuthHelper(String serverAddr, String namespace) {
        this.serverAddr = serverAddr;
        this.namespace = namespace;
    }

    public NacosAuthHelper(String serverAddr, String namespace, String username, String password) {
        this(serverAddr, namespace);
        this.username = username;
        this.password = password;
    }

    public NacosAuthHelper(String serverAddr, String namespace, String accessKey, String secretKey, String signType) {
        this(serverAddr, namespace);
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.signType = signType;
    }

    /**
     * 执行登录认证，获取accessToken
     *
     * @return true表示认证成功，false表示认证失败
     */
    public boolean login() {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            log.debug("Username or password not configured, skip login");
            return false;
        }

        try {
            String url = String.format("http://%s%s", serverAddr, LOGIN_PATH);
            url += "?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
            url += "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(DEFAULT_TIMEOUT);
            conn.setReadTimeout(DEFAULT_TIMEOUT);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String response = readResponse(conn);
                log.debug("Nacos login response: {}", response);

                TokenResponse tokenResponse = JacksonUtil.toObject(response, TokenResponse.class);
                if (tokenResponse != null && StrUtil.isNotBlank(tokenResponse.getAccessToken())) {
                    this.accessToken = tokenResponse.getAccessToken();
                    this.tokenExpireTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(tokenResponse.getTokenTtl() > 0 ? tokenResponse.getTokenTtl() : 18000);
                    this.isAuthenticated = true;
                    log.info("Nacos login successful, token will expire in {} seconds", tokenResponse.getTokenTtl());
                    return true;
                }
            }

            log.warn("Nacos login failed, response code: {}", responseCode);
            return false;

        } catch (Exception e) {
            log.error("Nacos login error", e);
            return false;
        }
    }

    /**
     * 检查token是否过期，需要刷新
     *
     * @return true表示需要刷新token
     */
    public boolean needRefreshToken() {
        if (!isAuthenticated) {
            return true;
        }
        if (StrUtil.isBlank(accessToken)) {
            return true;
        }
        return System.currentTimeMillis() >= (tokenExpireTime - TimeUnit.MINUTES.toMillis(5));
    }

    /**
     * 检查是否启用了认证
     *
     * @return true表示启用了认证
     */
    public boolean isAuthEnabled() {
        return StrUtil.isNotBlank(username) || StrUtil.isNotBlank(accessKey);
    }

    /**
     * 为HttpURLConnection添加认证信息
     *
     * @param conn HTTP连接
     */
    public void setAuthHeader(HttpURLConnection conn) {
        if (StrUtil.isNotBlank(accessToken)) {
            conn.setRequestProperty("accessToken", accessToken);
        }
    }

    /**
     * 为请求URL添加认证参数
     *
     * @param url 原始URL
     * @return 添加认证参数后的URL
     */
    public String addAuthParams(String url) {
        if (StrUtil.isNotBlank(accessToken)) {
            url += "&accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        }
        return url;
    }

    /**
     * 使用AccessKey/SecretKey生成签名
     *
     * @param data 需要签名的数据
     * @return 签名字符串
     */
    public String generateSignature(String data) {
        if (StrUtil.isBlank(secretKey)) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            log.error("Failed to generate signature", e);
            return "";
        }
    }

    /**
     * 为监听请求添加签名认证参数（v2.x长轮询）
     *
     * @param listeningConfigs 监听的配置内容
     * @return 添加签名后的内容
     */
    public String signListeningConfig(String listeningConfigs) {
        if (StrUtil.isBlank(accessKey) || StrUtil.isBlank(secretKey)) {
            return listeningConfigs;
        }

        String signature = generateSignature(listeningConfigs);
        String timestamp = String.valueOf(System.currentTimeMillis());

        StringBuilder signedBuilder = new StringBuilder(listeningConfigs);
        char separator = (char) 2;

        if (listeningConfigs.contains(String.valueOf(separator))) {
            String[] parts = listeningConfigs.split(String.valueOf(separator));
            if (parts.length >= 4) {
                signedBuilder = new StringBuilder();
                signedBuilder.append(parts[0]).append(separator);
                signedBuilder.append(parts[1]).append(separator);
                signedBuilder.append(parts[2]).append(separator);
                signedBuilder.append(parts[3]).append(separator);
                signedBuilder.append("accessKey=").append(accessKey).append(separator);
                signedBuilder.append("timestamp=").append(timestamp).append(separator);
                signedBuilder.append("signature=").append(signature);
            }
        }

        return signedBuilder.toString();
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
        }
    }

    /**
     * Token响应结构
     */
    public static class TokenResponse {
        private String accessToken;
        private String tokenType;
        private long tokenTtl;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public long getTokenTtl() {
            return tokenTtl;
        }

        public void setTokenTtl(long tokenTtl) {
            this.tokenTtl = tokenTtl;
        }
    }
}
