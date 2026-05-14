package io.github.lbkones.config.httpnacos;

import java.security.MessageDigest;


/**
 * 配置缓存
 */
public class ConfigCache {
    String content;
    String md5;
    private static final String CHARSET = "UTF-8";
    ConfigCache(String content) {
        this.content = content;
        this.md5 = calculateMD5(content);
    }

    private static String calculateMD5(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(content.getBytes(CHARSET));
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}