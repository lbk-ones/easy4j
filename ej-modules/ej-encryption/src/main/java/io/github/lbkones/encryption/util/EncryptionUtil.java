package io.github.lbkones.encryption.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lbkones.encryption.model.EncryptedRequest;
import io.github.lbkones.encryption.model.EncryptedResponse;
import io.github.lbkones.encryption.provider.EncryptionProvider;

/**
 * 加密处理工具类
 */
public class EncryptionUtil {

    private static final ObjectMapper objectMapper = EncryptionJson.getMapper();

    /**
     * 加密对象
     * @param object 要加密的对象
     * @param provider 加解密提供者
     * @param isMask 是否需要脱敏字段
     * @return 加密之后的对象
     * @param <T> 泛型约束
     */
    public static <T> EncryptedResponse encryptObject(T object, EncryptionProvider provider, boolean isMask) {
        if (object == null || provider == null) {
            return new EncryptedResponse(null);
        }

        try {
            if(isMask){
                // 先对字段进行脱敏
                object = MaskingUtil.maskFields(object);
            }
            String json = objectMapper.writeValueAsString(object);
            String encrypted = provider.encrypt(json);
            return new EncryptedResponse(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt object", e);
        }
    }

    /**
     * 解密对象
     */
    public static <T> T decryptObject(EncryptedRequest request, Class<T> targetClass, EncryptionProvider provider) {
        if (request == null || request.getData() == null || provider == null) {
            return null;
        }

        try {
            String decrypted = provider.decrypt(request.getData());
            return objectMapper.readValue(decrypted, targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt object", e);
        }
    }

    /**
     * 加密字符串
     */
    public static String encrypt(String plaintext, EncryptionProvider provider) {
        if (plaintext == null || provider == null) {
            return plaintext;
        }
        return provider.encrypt(plaintext);
    }

    /**
     * 解密字符串
     */
    public static String decrypt(String ciphertext, EncryptionProvider provider) {
        if (ciphertext == null || provider == null) {
            return ciphertext;
        }
        return provider.decrypt(ciphertext);
    }
}
