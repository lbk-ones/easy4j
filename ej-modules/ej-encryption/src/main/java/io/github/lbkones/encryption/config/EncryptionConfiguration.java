package io.github.lbkones.encryption.config;

import cn.hutool.core.util.ServiceLoaderUtil;
import cn.hutool.core.util.StrUtil;
import io.github.lbkones.encryption.advice.EncryptionResponseBodyAdvice;
import io.github.lbkones.encryption.filter.DecryptionFilter;
import io.github.lbkones.encryption.provider.EncryptionProvider;
import io.github.lbkones.encryption.provider.EncryptionProviderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

/**
 * 加密模块配置类
 */
@Configuration
@ConditionalOnProperty(prefix = "easy4j.encryption", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({EncryptionProperties.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class EncryptionConfiguration {

    private final EncryptionProperties encryptionProperties;

    public EncryptionConfiguration(EncryptionProperties encryptionProperties) throws Exception {
        this.encryptionProperties = encryptionProperties;
        initializeEncryptionProviders();
    }

    /**
     * 初始化加密提供者
     */
    private void initializeEncryptionProviders() throws Exception {
        List<EncryptionProvider> load = ServiceLoaderUtil.loadList(EncryptionProvider.class);
        for (EncryptionProvider encryptionProvider : load) {
            String name = encryptionProvider.getName();
            if(StrUtil.isNotBlank(name)) {
                encryptionProvider.setPrivateKey(this.encryptionProperties.getPrivateKey());
                encryptionProvider.setPublicKey(this.encryptionProperties.getPublicKey());
                EncryptionProviderFactory.register(name , encryptionProvider);
            }
        }
    }

    /**
     * 注册请求解密过滤器
     */
    @Bean
    public FilterRegistrationBean<DecryptionFilter> decryptionFilterRegistrationBean(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping requestMappingHandlerMapping) {
        FilterRegistrationBean<DecryptionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new DecryptionFilter(encryptionProperties, requestMappingHandlerMapping));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    /**
     * 注册响应加密 Advice
     */
    @Bean
    public EncryptionResponseBodyAdvice encryptionResponseBodyAdvice() {
        return new EncryptionResponseBodyAdvice(encryptionProperties);
    }
}
