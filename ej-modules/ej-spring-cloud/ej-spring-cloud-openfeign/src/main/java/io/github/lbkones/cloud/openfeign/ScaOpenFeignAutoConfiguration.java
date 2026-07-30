package io.github.lbkones.cloud.openfeign;

import easy4j.infra.common.utils.json.JacksonUtil;
import feign.RequestInterceptor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;


@Configuration
public class ScaOpenFeignAutoConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new OpenFeignRequestInterceptor();
    }

    // 配置 RestTemplate
    // 这里使用一下 openfeign的 hc5
    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        initJackson(restTemplate); // 保持原有Jackson配置
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingRequestInterceptor())); // 保持原有拦截器

        return restTemplate;
    }

    private static void initJackson(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.removeIf(e -> e instanceof MappingJackson2HttpMessageConverter);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(JacksonUtil.getMapper());
        messageConverters.add(0, mappingJackson2HttpMessageConverter);
        restTemplate.setMessageConverters(messageConverters);
    }

}
