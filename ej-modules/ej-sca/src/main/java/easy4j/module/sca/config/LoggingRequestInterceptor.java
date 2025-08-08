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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * LoggingRequestInterceptor
 * 日志拦截器
 *
 * @author bokun.li
 * @date 2025/8/8
 */
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long begintime = System.currentTimeMillis();
        // 打印请求信息
        logRequest(request, body);

        // 执行请求并获取响应
        ClientHttpResponse response = execution.execute(request, body);
        long endTime = System.currentTimeMillis();

        // 打印响应信息
        logResponse(response,(endTime-begintime));

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.debug("=== REST请求开始 === "+request.getMethod()+":"+request.getURI());
        log.debug("=== 请求头: {}", request.getHeaders());
        //log.debug("请求体: {}", new String(body, StandardCharsets.UTF_8));
    }

    private void logResponse(ClientHttpResponse response,long duTime) throws IOException {
        log.debug("=== 响应头: {}", response.getHeaders());
        log.debug("=== REST请求完成 === 耗时"+duTime+"ms 状态码:"+response.getStatusCode());
        //log.debug("响应体: {}", StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
        //log.debug("=== 响应接收完成 ===");
    }
}
