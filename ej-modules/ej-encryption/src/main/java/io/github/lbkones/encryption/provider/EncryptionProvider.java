package io.github.lbkones.encryption.provider;

/**
 * 加密提供者接口，支持扩展不同的加密方式
 */
public interface EncryptionProvider {

    /**
     * 获取加密方式名称
     */
    String getName();

    /**
     * 加密
     */
    String encrypt(String plaintext);

    /**
     * 解密
     */
    String decrypt(String ciphertext);
}
