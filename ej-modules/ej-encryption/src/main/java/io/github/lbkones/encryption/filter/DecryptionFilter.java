package io.github.lbkones.encryption.filter;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lbkones.encryption.annotation.NoEncrypt;
import io.github.lbkones.encryption.config.EncryptionProperties;
import io.github.lbkones.encryption.model.EncryptedRequest;
import io.github.lbkones.encryption.provider.EncryptionProvider;
import io.github.lbkones.encryption.provider.EncryptionProviderFactory;
import io.github.lbkones.encryption.util.EncryptionJson;
import io.github.lbkones.pure.ReplacedBodyRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 请求参数解密过滤器
 */
public class DecryptionFilter implements Filter {

    private final EncryptionProperties encryptionProperties;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper = EncryptionJson.getMapper();

    public DecryptionFilter(EncryptionProperties encryptionProperties,
                          RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.encryptionProperties = encryptionProperties;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!encryptionProperties.isEnabled() || encryptionProperties.isDisabledApiEnc() || !(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // 检查是否需要解密
        if (!needsDecryption(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 读取请求体
            String requestBody = ReplacedBodyRequestWrapper.readRequestBody(httpRequest);

            if (requestBody.isEmpty()) {
                chain.doFilter(request, response);
                return;
            }

            // 解密请求体
            String decryptedBody = decryptRequestBody(requestBody);

            // 包装请求
            HttpServletRequest wrappedRequest = wrapRequest(httpRequest, decryptedBody);
            chain.doFilter(wrappedRequest, response);
        } catch (Exception e) {
            throw new ServletException("Decryption failed", e);
        }
    }

    /**
     * 检查是否需要解密
     */
    private boolean needsDecryption(HttpServletRequest request) {
        try {
            Object handler = Objects.requireNonNull(requestMappingHandlerMapping.getHandler(request)).getHandler();
            if (handler instanceof HandlerMethod handlerMethod) {

                // 检查方法上的 @NoEncrypt
                NoEncrypt methodAnnotation = handlerMethod.getMethodAnnotation(NoEncrypt.class);
                if (methodAnnotation != null) {
                    return false;
                }
                Class<?> beanType = handlerMethod.getBeanType();
                String skipList = encryptionProperties.getSkipList();
                List<String> split = StrUtil.split(skipList, StrPool.COMMA);
                String name = beanType.getName();
                for (String s : split) {
                    if(StrUtil.startWith(name,s)){
                        return false;
                    }
                }

                // 检查类上的 @NoEncrypt
                NoEncrypt classAnnotation = beanType.getAnnotation(NoEncrypt.class);
                return classAnnotation == null;
            }
            return true;
        } catch (Exception e) {
            // 如果获取 handler 失败，就不解密了
            return false;
        }
    }

    /**
     * 解密请求体
     */
    private String decryptRequestBody(String encryptedData) throws Exception {
        EncryptionProvider provider = EncryptionProviderFactory.get(encryptionProperties.getEncryptionType());
        if (provider == null) {
            throw new RuntimeException("Encryption provider not found: " + encryptionProperties.getEncryptionType());
        }

        // 解析加密的请求
        EncryptedRequest encryptedRequest = objectMapper.readValue(encryptedData, EncryptedRequest.class);

        // 解密数据
        return provider.decrypt(encryptedRequest.getData());
    }

    /**
     * 包装请求
     */
    private HttpServletRequest wrapRequest(HttpServletRequest request, String decryptedBody) {
        return new ReplacedBodyRequestWrapper(request,decryptedBody);
    }
}
