package io.github.lbkones.encryption.advice;

import io.github.lbkones.encryption.annotation.NoEncrypt;
import io.github.lbkones.encryption.config.EncryptionProperties;
import io.github.lbkones.encryption.provider.EncryptionProvider;
import io.github.lbkones.encryption.provider.EncryptionProviderFactory;
import io.github.lbkones.encryption.util.EncryptionUtil;
import io.github.lbkones.encryption.util.MaskingUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应加密和字段脱敏 Advice
 */
@RestControllerAdvice
public class EncryptionResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final EncryptionProperties encryptionProperties;

    public EncryptionResponseBodyAdvice(EncryptionProperties encryptionProperties) {
        this.encryptionProperties = encryptionProperties;
    }

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        if (!encryptionProperties.isEnabled()) {
            return false;
        }

        // 检查是否标记了 @NoEncrypt
        NoEncrypt methodAnnotation = returnType.getMethodAnnotation(NoEncrypt.class);
        if (methodAnnotation != null) {
            return false;
        }

        NoEncrypt classAnnotation = returnType.getContainingClass().getAnnotation(NoEncrypt.class);
        return classAnnotation == null;
    }

    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        if (body == null) {
            return null;
        }

        // 获取加密提供者
        EncryptionProvider provider = EncryptionProviderFactory.get(encryptionProperties.getEncryptionType());
        if (provider == null) {
            return body;
        }

        // 先进行字段脱敏
        body = MaskingUtil.maskFields(body);

        // 然后进行加密
        return EncryptionUtil.encryptObject(body, provider);
    }
}
