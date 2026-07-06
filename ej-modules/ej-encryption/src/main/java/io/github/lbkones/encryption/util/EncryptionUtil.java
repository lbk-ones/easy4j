package io.github.lbkones.encryption.util;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lbkones.encryption.annotation.NoEncrypt;
import io.github.lbkones.encryption.config.EncryptionProperties;
import io.github.lbkones.encryption.model.EncryptedRequest;
import io.github.lbkones.encryption.model.EncryptedResponse;
import io.github.lbkones.encryption.provider.EncryptionProvider;
import io.github.lbkones.encryption.provider.EncryptionProviderFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Objects;

/**
 * 加密处理工具类
 */
public class EncryptionUtil {

    private static final ObjectMapper objectMapper = EncryptionJson.getMapper();

    /**
     * 加密对象
     *
     * @param object   要加密的对象
     * @param provider 加解密提供者
     * @param isMask   是否需要脱敏字段
     * @param <T>      泛型约束
     * @return 加密之后的对象
     */
    public static <T> EncryptedResponse encryptObject(T object, EncryptionProvider provider, boolean isMask) {
        if (object == null || provider == null) {
            return new EncryptedResponse(null);
        }

        try {
            if (isMask) {
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

    /**
     * 自动根据全局配置来判断是否应该加密
     */
    public static Object autoEncrypt(Object object, HttpServletRequest request) {
        if (object == null) return object;
        EncryptionProperties encryptionProperties = SUtils.getEncryptProperties();
        EncryptionProvider provider = EncryptionProviderFactory.get(encryptionProperties.getEncryptionType());
        // 如果这个字段为false 那么禁用字段脱敏，禁用接口加解密
        if (!encryptionProperties.isEnabled()) {
            return object;
        }
        // 先进行字段脱敏
        object = MaskingUtil.maskFields(object);
        // 如果禁用了 接口加解密 那么只保留响应字段脱敏功能
        if (encryptionProperties.isDisabledApiEnc()) {
            return object;
        }
        if (needEncrypt(request) && provider != null) {
            // 然后进行加密
            return EncryptionUtil.encryptObject(object, provider, false);
        } else {
            return object;
        }

    }

    /**
     * 判断这个请求是否应该加密
     *
     * @param request 请求request
     * @return boolean
     */
    public static boolean needEncrypt(HttpServletRequest request) {
        try {
            Object mappingHandlerMapping = SUtils.getBean("requestMappingHandlerMapping");
            if (mappingHandlerMapping instanceof RequestMappingHandlerMapping requestMappingHandlerMapping) {
                Object handler = Objects.requireNonNull(requestMappingHandlerMapping.getHandler(request)).getHandler();
                if (handler instanceof HandlerMethod handlerMethod) {
                    // 检查方法上的 @NoEncrypt
                    NoEncrypt methodAnnotation = handlerMethod.getMethodAnnotation(NoEncrypt.class);
                    if (methodAnnotation != null) {
                        return false;
                    }
                    EncryptionProperties encryptionProperties = SUtils.getEncryptProperties();
                    Class<?> beanType = handlerMethod.getBeanType();
                    String skipList = encryptionProperties.getSkipList();
                    List<String> split = StrUtil.split(skipList, StrPool.COMMA);
                    String name = beanType.getName();
                    for (String s : split) {
                        if (StrUtil.startWith(name, s)) {
                            return false;
                        }
                    }
                    // 检查类上的 @NoEncrypt
                    NoEncrypt classAnnotation = beanType.getAnnotation(NoEncrypt.class);
                    return classAnnotation == null;
                }
                return true;
            }
            return false;

        } catch (Exception e) {
            // 如果获取 handler 失败，就不解密了
            return false;
        }
    }
}
