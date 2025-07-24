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
package easy4j.module.sca.broadcast;

import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BroadcastFeignClient
 *
 * @author bokun.li
 * @date 2025/7/16
 */
public class BroadcastFeignClient implements Client {


    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Client delegate;
    private final DiscoveryClient discoveryClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public BroadcastFeignClient(Client delegate, DiscoveryClient discoveryClient) {
        this.delegate = delegate;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        // from URL get serviceName
        String serviceName = extractServiceName(request.url());

        // get serverinstance by serviceName
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        Map<String, Response> responses = new HashMap<>();

        CompletableFuture.allOf(instances.stream()
                .map(instance -> CompletableFuture.runAsync(() -> {
                    String instanceUrl = getInstanceUrl(request, instance);
                    try {
                        Request instanceRequest = buildInstanceRequest(request, instance);
                        Response response = delegate.execute(instanceRequest, options);
                        responses.put(instance.getInstanceId(), response);
                    } catch (Throwable e) {
                        logger.error("broadcast 【" + instanceUrl + "】feign,exception -->", e);
                        responses.put(instance.getInstanceId(), null);
                    }
                }, executorService)).toArray(CompletableFuture[]::new)).join();

        return buildAggregatedResponse(responses);
    }

    private String extractServiceName(String url) {
        // 从 URL 中提取服务名，例如：http://service-name/path -> service-name
        URI uri = URI.create(url);
        return uri.getHost();
    }

    private String getInstanceUrl(Request originalRequest, ServiceInstance instance) {
        String originalUrl = originalRequest.url();
        return instance.getUri() + originalUrl.substring(originalUrl.indexOf('/', 8));

    }

    private Request buildInstanceRequest(Request originalRequest, ServiceInstance instance) {
        String instanceUrl = getInstanceUrl(originalRequest, instance);

        return Request.create(
                originalRequest.httpMethod(),
                instanceUrl,
                originalRequest.headers(),
                originalRequest.body(),
                originalRequest.charset(),
                originalRequest.requestTemplate()
        );
    }

    private Response buildAggregatedResponse(Map<String, Response> responses) {
        // 这里简化处理，返回第一个成功的响应或默认错误响应
        return responses.values().stream()
                .filter(r -> r != null && r.status() >= 200 && r.status() < 300)
                .findFirst()
                .orElse(Response.builder()
                        .status(500)
                        .reason("Broadcast failed for all instances")
                        .build());
    }
}