package io.github.lbkones.encryption.provider;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加密提供者实现
 * 后端使用私钥用于：
 * 1. 解密客户端发送的请求数据（客户端使用公钥加密）
 * 2. 加密服务端返回的响应数据
 */
public class RsaEncryptionProvider implements EncryptionProvider {

    private final PrivateKey privateKey;

    public RsaEncryptionProvider(String privateKeyStr) throws Exception {
        this.privateKey = loadPrivateKey(privateKeyStr);
    }

    @Override
    public String getName() {
        return "rsa";
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty() || privateKey == null) {
            return plaintext;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty() || privateKey == null) {
            return ciphertext;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decoded = Base64.getDecoder().decode(ciphertext);
            byte[] plaintext = cipher.doFinal(decoded);
            return new String(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("RSA decryption failed", e);
        }
    }

    private PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        if (privateKeyStr == null || privateKeyStr.isEmpty()) {
            return null;
        }
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}

