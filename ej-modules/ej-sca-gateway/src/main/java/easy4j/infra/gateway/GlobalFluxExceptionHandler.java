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
package easy4j.infra.gateway;

import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.header.GateWayEasyResult;
import easy4j.infra.common.utils.json.JacksonUtil;
import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * GlobalFluxExceptionHandler
 * 异常拦截器
 *
 * @author bokun.li
 */
@Slf4j
public class GlobalFluxExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof ConnectTimeoutException) {
            log.error("连接异常--->" + ex.getMessage());
        } else {
            log.error("出现未知异常--->" + ex.getMessage());
        }
        byte[] bytes = JacksonUtil.toJsonContainNull(GateWayEasyResult.errorGateway(ex)).getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse()
                .writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    return bufferFactory.wrap(bytes);
                }));
    }

}
