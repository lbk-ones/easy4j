package io.github.lbkones.config.httpnacos;

import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * ==================== 版本检测器 ====================
 */
/**
 * Nacos版本检测
 */
public class NacosVersionDetector {

    @Getter
    public enum NacosVersion {
        V1_X("1.x", 1),
        V2_X("2.x", 2),
        V3_X("3.x", 3),
        UNKNOWN("unknown", -1);

        private final String versionName;
        private final int majorVersion;

        NacosVersion(String versionName, int majorVersion) {
            this.versionName = versionName;
            this.majorVersion = majorVersion;
        }

    }

    private final String serverAddr;

    public NacosVersionDetector(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    /**
     * 检测Nacos服务器版本
     */
    public NacosVersion detect() {
        try {
            // 尝试获取版本信息
            String url = String.format("http://%s/nacos/v1/ns/operator/metrics", serverAddr);
            String response = sendSimpleGet(url);

            if (response != null && response.contains("\"version\"")) {
                String version = extractVersion(response);
                return parseVersion(version);
            }

            // 备选方案：尝试v2接口
            url = String.format("http://%s/nacos/v2/core/ops/metrics", serverAddr);
            response = sendSimpleGet(url);
            if (response != null) {
                return NacosVersion.V2_X;
            }

            return NacosVersion.UNKNOWN;
        } catch (Exception e) {
            System.err.println("Failed to detect version: " + e.getMessage());
            return NacosVersion.UNKNOWN;
        }
    }

    private String extractVersion(String response) {
        // 从JSON中提取版本号
        int startIdx = response.indexOf("\"version\":");
        if (startIdx == -1) return "";

        int quoteIdx = response.indexOf("\"", startIdx + 10);
        int endIdx = response.indexOf("\"", quoteIdx + 1);

        if (endIdx > quoteIdx) {
            return response.substring(quoteIdx + 1, endIdx);
        }
        return "";
    }

    private NacosVersion parseVersion(String versionStr) {
        if (versionStr.startsWith("2.")) {
            return NacosVersion.V2_X;
        } else if (versionStr.startsWith("3.")) {
            return NacosVersion.V3_X;
        } else if (versionStr.startsWith("1.")) {
            return NacosVersion.V1_X;
        }
        return NacosVersion.UNKNOWN;
    }

    private String sendSimpleGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            if (conn.getResponseCode() == 200) {
                return readInputStream(conn.getInputStream());
            }
        } finally {
            conn.disconnect();
        }
        return null;
    }

    private String readInputStream(InputStream is) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toString("UTF-8");
        }
    }
}
