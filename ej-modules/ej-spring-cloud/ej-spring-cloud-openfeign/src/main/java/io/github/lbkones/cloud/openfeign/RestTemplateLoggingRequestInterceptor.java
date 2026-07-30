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
package io.github.lbkones.cloud.openfeign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * RestTemplate的日志拦截器
 *
 * @author bokun.li
 * @since 2.1.4
 */
public class RestTemplateLoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateLoggingRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long begintime = System.currentTimeMillis();
        // 打印请求信息
        logRequest(request);

        // 执行请求并获取响应
        ClientHttpResponse response = execution.execute(request, body);
        long endTime = System.currentTimeMillis();

        // 打印响应信息
        logResponse(response,(endTime-begintime));

        return response;
    }

    private void logRequest(HttpRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("REST request begin-> method:{}，uri:{}，headers:{}",request.getMethod(),request.getURI(),request.getHeaders());
        }
    }

    private void logResponse(ClientHttpResponse response,long duTime) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("REST request end-> cost:{}ms，statusCode:{}，headers:{}",duTime,response.getStatusCode(), response.getHeaders());
        }

    }
}
