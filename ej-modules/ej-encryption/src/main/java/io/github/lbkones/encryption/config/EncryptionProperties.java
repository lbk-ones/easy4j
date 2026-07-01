package io.github.lbkones.encryption.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "easy4j.encryption")
public class EncryptionProperties {

    /**
     * 是否启用加解密功能
     */
    private boolean enabled = false;

    /**
     * RSA私钥（用于解密请求和加密响应）
     */
    private String privateKey;

    /**
     * 加密方式，默认为rsa
     */
    private String encryptionType = "rsa";
}
