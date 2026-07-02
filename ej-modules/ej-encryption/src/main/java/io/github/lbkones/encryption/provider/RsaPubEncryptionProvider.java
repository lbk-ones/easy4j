package io.github.lbkones.encryption.provider;

import cn.hutool.core.util.StrUtil;
import io.github.lbkones.encryption.enums.EncryptProviderType;
import io.github.lbkones.encryption.util.SUtils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA公钥加密解密
 */
public class RsaPubEncryptionProvider implements EncryptionProvider {

    private PublicKey publicKey;


    @Override
    public void setPrivateKey(String key) {

    }
    @Override
    public void setPublicKey(String key) {
        try {
            this.publicKey = loadPublicKey(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getName() {
        return EncryptProviderType.RSA_PUBLIC.getCode();
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty() || publicKey == null) {
            return plaintext;
        }
        try {
            byte[] bytes = encryptLongData(plaintext.getBytes(), SUtils.getEncryptProperties().getRsaBlockSize()-11);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty() || publicKey == null) {
            return ciphertext;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);
            byte[] plaintext = decryptLongData(decoded, SUtils.getEncryptProperties().getRsaBlockSize());
            return new String(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("RSA decryption failed", e);
        }
    }

    private PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        if (publicKeyStr == null || publicKeyStr.isEmpty()) {
            return null;
        }
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 分块加密长数据
     * @param data 原文数据
     * @param blockSize 分块大小（RSA-2048 为 256） 1024 为 117
     */
    private byte[] encryptLongData(byte[] data, int blockSize)
            throws Exception {
        int dataLength = data.length;
        // 默认P1
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 如果数据不超过块大小，直接加密
        if (dataLength <= blockSize) {
            return cipher.doFinal(data);
        }
        // 分块加密
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        int offset = 0;

        while (offset < dataLength) {
            int len = Math.min(blockSize, dataLength - offset);
            byte[] encryptedBlock = cipher.doFinal(data, offset, len);
            baos.write(encryptedBlock);
            offset += len;
        }

        return baos.toByteArray();
    }

    /**
     * 分块解密长数据
     * @param data 密文数据
     * @param blockSize 分块大小（RSA-2048 为 256）
     */
    private byte[] decryptLongData( byte[] data, int blockSize)
            throws Exception {
        // 默认P1
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        int dataLength = data.length;

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        int offset = 0;

        while (offset < dataLength) {
            int len = Math.min(blockSize, dataLength - offset);
            byte[] decryptedBlock = cipher.doFinal(data, offset, len);
            baos.write(decryptedBlock);
            offset += len;
        }

        return baos.toByteArray();
    }
}

