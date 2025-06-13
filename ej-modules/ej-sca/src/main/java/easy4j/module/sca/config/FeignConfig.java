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
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import javax.annotation.Resource;

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
    public CustomSentinelExceptionHandler customSentinelExceptionHandler() {
        return new CustomSentinelExceptionHandler();
    }


    @Bean
    public DefaultRequestOriginParser defaultRequestOriginParser() {
        return new DefaultRequestOriginParser();
    }

    @Bean
    public ScaRunner scaRunner() {
        return new ScaRunner();
    }

}
