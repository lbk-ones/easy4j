package io.github.lbkones.encryption.enums;

import lombok.Getter;

/**
 * 内置加解密提供者
 */
@Getter
public enum EncryptProviderType {

    // rsa 用私钥来加解密
    RSA_PRIVATE("rsa-private"),
    // rsa 用公钥来加解密
    RSA_PUBLIC("rsa-public"),
    ;
    private final String code;

    EncryptProviderType(String code) {
        this.code = code;
    }
}
