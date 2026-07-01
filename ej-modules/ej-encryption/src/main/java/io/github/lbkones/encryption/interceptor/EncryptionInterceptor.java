package io.github.lbkones.encryption.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lbkones.encryption.annotation.NoEncrypt;
import io.github.lbkones.encryption.config.EncryptionProperties;
import io.github.lbkones.encryption.provider.EncryptionProvider;
import io.github.lbkones.encryption.provider.EncryptionProviderFactory;
import io.github.lbkones.encryption.util.EncryptionJson;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * MVC加密拦截器
 */
public class EncryptionInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = EncryptionJson.getMapper();
    private final EncryptionProperties encryptionProperties;

    public EncryptionInterceptor(EncryptionProperties encryptionProperties) {
        this.encryptionProperties = encryptionProperties;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!encryptionProperties.isEnabled()) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查是否标记了 @NoEncrypt
        if (isNoEncrypt(handlerMethod)) {
            return true;
        }

        return true;
    }

    /**
     * 检查是否标记了 @NoEncrypt 注解
     */
    private boolean isNoEncrypt(HandlerMethod handlerMethod) {
        NoEncrypt methodAnnotation = handlerMethod.getMethodAnnotation(NoEncrypt.class);
        if (methodAnnotation != null) {
            return true;
        }

        NoEncrypt classAnnotation = handlerMethod.getBeanType().getAnnotation(NoEncrypt.class);
        return classAnnotation != null;
    }

    /**
     * 获取加密提供者
     */
    protected EncryptionProvider getEncryptionProvider() {
        String encryptionType = encryptionProperties.getEncryptionType();
        return EncryptionProviderFactory.get(encryptionType);
    }
}
