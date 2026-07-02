package io.github.lbkones.encryption.config;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.sun.source.doctree.IndexTree;
import io.github.lbkones.encryption.enums.EncryptProviderType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@Component
@ConfigurationProperties(prefix = "easy4j.encryption")
public class EncryptionProperties {

    /**
     * 如果为false 则关闭接口返参入参加解密
     */
    private boolean enabled = false;

    /**
     * 是否禁用接口加解密，true代表不需要接口加解密，false代表需要接口加解密，默认为true
     */
    private boolean disabledApiEnc = true;

    /**
     * RSA私钥（用于解密请求和加密响应）
     */
    private String privateKey;

    /**
     * RSA公钥（用于解密请求和加密响应）
     */
    private String publicKey;

    /**
     * 加密方式，默认为 rsa-private
     */
    private String encryptionType = EncryptProviderType.RSA_PRIVATE.getCode();


    /**
     * RSA-1024 P1填充 128
     * RSA-2048 P1填充 256
     * rsa算法分段加解密的块大小
     */
    private Integer rsaBlockSize = 256;

    /**
     * 不需要加解密的接口所在类包名的前缀,多个用逗号分隔
     */
    private String skipList;

    public String getSkipList() {
        return String.join(StrPool.COMMA, Stream.of(
                "com.alibaba.druid",
                "com.github.xiaoymin.knife4j",
                "easy4j.",
                "io.github.lbkones.",
                "org.springdoc.",
                "de.codecentric.boot.admin.",
                skipList
        ).filter(StrUtil::isNotBlank).toList());
    }
}
