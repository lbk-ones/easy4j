/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.sca.config;

import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.module.sca.annotations.EnableSca;
import easy4j.module.sca.handler.CustomSentinelExceptionHandler;
import easy4j.module.sca.interceptor.DefaultRequestOriginParser;
import easy4j.module.sca.interceptor.Easy4jRequestInterceptor;
import easy4j.module.sca.runner.ScaRunner;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * FeignConfig
 *
 * @author bokun.li
 * @date 2025-05
 */
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
@Slf4j
@Configuration
@EnableSca
public class FeignConfig {
    @Resource
    EjSysProperties ejSysProperties;


    /**
     * 设置feign header参数
     * 【X_ACCESS_TOKEN】【X_SIGN】【X_TIMESTAMP】
     *
     * @return
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new Easy4jRequestInterceptor();
    }


    /**
     * Feign 客户端的日志记录，默认级别为NONE
     * Logger.Level 的具体级别如下：
     * NONE：不记录任何信息
     * BASIC：仅记录请求方法、URL以及响应状态码和执行时间
     * HEADERS：除了记录 BASIC级别的信息外，还会记录请求和响应的头信息
     * FULL：记录所有请求与响应的明细，包括头信息、请求体、元数据
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Feign支持文件上传
     *
     * @param messageConverters
     * @return
     */
    @Bean
    @Primary
    @Scope("prototype")
    public Encoder multipartFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    // 全局Sentinel自定义信息处理
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public CustomSentinelExceptionHandler customSentinelExceptionHandler() {
        return new CustomSentinelExceptionHandler();
    }


    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public DefaultRequestOriginParser defaultRequestOriginParser() {
        return new DefaultRequestOriginParser();
    }

    @Bean
    public ScaRunner scaRunner() {
        return new ScaRunner();
    }


    @Bean
    public NamingServerInvoker namingServerInvoker() {
        return NamingServerInvoker.createByEnv(null);
    }


    @Bean
    public NacosEventListener nacosEventListener() {
        return new NacosEventListener();
    }


    /**
     * 配置HTTP连接池管理器
     */
    /*@Bean
    public PoolingHttpClientConnectionManager connectionManager() {
        Integer maxTotal = Easy4j.getProperty(SysConstant.PARAM_PREFIX + SP.DOT + "rest-http-pool-max-total", Integer.class, 200);
        Integer perRoute = Easy4j.getProperty(SysConstant.PARAM_PREFIX + SP.DOT + "rest-http-default-max-per-route", Integer.class, 160);
        Integer validateAfterInactivity = Easy4j.getProperty(SysConstant.PARAM_PREFIX + SP.DOT + "rest-http-validate-after-inactivity", Integer.class, 2000);
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        // 连接池最大连接数
        connectionManager.setMaxTotal(maxTotal);

        // 每个路由的最大连接数（路由 = 协议 + 主机 + 端口）
        connectionManager.setDefaultMaxPerRoute(perRoute);
        connectionManager.setValidateAfterInactivity(validateAfterInactivity); // 连接空闲多久后校验有效性

        // 可为特定路由设置单独的最大连接数（如对某个服务需要更多连接）
        // HttpHost host = new HttpHost("api.example.com", 80);
        // connectionManager.setMaxPerRoute(new HttpRoute(host), 50);

        return connectionManager;
    }

    *//**
     * 配置HttpClient（使用连接池）
     *//*
    @Bean
    public HttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        Integer connectTimeOut = Easy4j.getProperty(SysConstant.PARAM_PREFIX + SP.DOT + "rest-http-connect-timeout", Integer.class, 3000);
        Integer connectionRequestTimeOUt = Easy4j.getProperty(SysConstant.PARAM_PREFIX + SP.DOT + "rest-http-connection-request-timeout", Integer.class, 5000);
        Integer socketTimeout = Easy4j.getProperty(SysConstant.PARAM_PREFIX + SP.DOT + "rest-http-socket-timeout", Integer.class, 10000);
        // 请求超时配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeOut)        // 建立连接超时时间（毫秒）
                .setConnectionRequestTimeout(connectionRequestTimeOUt)  // 从连接池获取连接的等待时间（毫秒）
                .setSocketTimeout(socketTimeout)         // 数据传输超时时间（毫秒）
                .build();

        // 构建HttpClient并关联连接池和超时配置
        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .build();
    }

    */

    /**
     * 创建带有连接池的RestTemplate
     *//*
    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
        // 使用HttpClient作为底层连接工厂
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        initJackson(restTemplate);
        restTemplate.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));

        return restTemplate;
    }*/

    /**
     * 配置HTTP连接池管理器（HttpClient 5.x 版本）
     */
    @Bean
    public PoolingHttpClientConnectionManager connectionManager() {
        // 从配置获取参数（保持原有逻辑）
        Integer maxTotal = Easy4j.getProperty(SysConstant.PARAM_PREFIX + ".rest-http-pool-max-total", Integer.class, 200);
        Integer perRoute = Easy4j.getProperty(SysConstant.PARAM_PREFIX + ".rest-http-default-max-per-route", Integer.class, 160);
        Integer validateAfterInactivity = Easy4j.getProperty(SysConstant.PARAM_PREFIX + ".rest-http-validate-after-inactivity", Integer.class, 2000);

        // 连接配置（如超时、缓冲区等）
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(3000)) // 连接超时（与RequestConfig的连接超时区别：此处是底层TCP连接超时）
                .setSocketTimeout(Timeout.ofMilliseconds(10000)) //  socket超时（数据传输超时）
                .setValidateAfterInactivity(TimeValue.of(validateAfterInactivity, TimeUnit.MILLISECONDS))
                .build();

        // 构建连接池管理器（HttpClient 5.x 用 PoolingHttpClientConnectionManagerBuilder）
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(maxTotal) // 连接池最大连接数
                .setMaxConnPerRoute(perRoute) // 每个路由默认最大连接数
                .setDefaultConnectionConfig(connectionConfig) // 默认连接配置
                //.setValidateAfterInactivity(TimeValue.of(validateAfterInactivity, TimeUnit.MILLISECONDS)) // 空闲连接校验时间
                .build();
    }

    /**
     * 配置HttpClient（HttpClient 5.x 版本）
     */
    @Bean
    public HttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        // 从配置获取参数（保持原有逻辑）
        Integer connectTimeOut = Easy4j.getProperty(SysConstant.PARAM_PREFIX + ".rest-http-connect-timeout", Integer.class, 3000);
        Integer connectionRequestTimeOut = Easy4j.getProperty(SysConstant.PARAM_PREFIX + ".rest-http-connection-request-timeout", Integer.class, 5000);
        Integer socketTimeout = Easy4j.getProperty(SysConstant.PARAM_PREFIX + ".rest-http-socket-timeout", Integer.class, 10000);

        // 请求配置（HttpClient 5.x 用 RequestConfig 替代旧的 RequestConfig）
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeOut)) // 建立连接超时
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionRequestTimeOut)) // 从连接池获取连接的等待超时
                .setResponseTimeout(Timeout.ofMilliseconds(socketTimeout)) // 响应超时（替代旧的 socketTimeout）
                .build();

        // 构建HttpClient（HttpClient 5.x 用 HttpClientBuilder）
        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager) // 关联连接池
                .setDefaultRequestConfig(requestConfig) // 设置默认请求配置
                .evictExpiredConnections() // 自动清理过期连接
                .evictIdleConnections(TimeValue.of(30, TimeUnit.SECONDS)) // 自动清理空闲超过30秒的连接（新增优化）
                .build();
    }

    /**
     * 创建带有连接池的RestTemplate（适配HttpClient 5.x）
     */
    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
        // 注意：Spring 5+ 已支持 HttpClient 5.x，需使用对应的 HttpComponentsClientHttpRequestFactory
        // 确保Spring版本 >= 5.3（Spring Boot 3.x 基础Spring版本为6.x，完全兼容）
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        initJackson(restTemplate); // 保持原有Jackson配置
        restTemplate.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor())); // 保持原有拦截器

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
