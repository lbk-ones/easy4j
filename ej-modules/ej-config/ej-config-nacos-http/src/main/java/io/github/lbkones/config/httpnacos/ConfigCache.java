package io.github.lbkones.config.httpnacos;

import cn.hutool.crypto.digest.DigestUtil;

import java.nio.charset.StandardCharsets;


/**
 * 配置缓存
 */
public class ConfigCache {
    String content;
    String md5;
    ConfigCache(String content) {
        this.content = content;
        this.md5 = calculateMD5(content);
    }

    private static String calculateMD5(String content) {
        try {
            return DigestUtil.md5Hex(content,StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}