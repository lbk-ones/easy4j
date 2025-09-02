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
import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalWebFluxExceptionHandler
 *
 * @author bokun.li
 * @date 2025/9/2
 */
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Slf4j
public class GlobalWebFluxExceptionHandler {

    // 连接异常
    @ExceptionHandler(ConnectTimeoutException.class)
    public EasyResult<Object> handleConnectTimeout(ConnectTimeoutException ex) {
        String message = ex.getMessage();
        log.error("连接异常--->" + message);
        return EasyResult.error(ex);
    }

    @ExceptionHandler(Exception.class)
    public EasyResult<Object> handleOtherExceptions(Exception ex) {
        log.error("未知异常--->" + ex.getMessage());
        return EasyResult.error(ex);
    }
}
