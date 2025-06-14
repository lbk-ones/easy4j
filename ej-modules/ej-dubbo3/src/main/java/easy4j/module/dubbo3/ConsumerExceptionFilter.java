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
package easy4j.module.dubbo3;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import easy4j.infra.common.header.EasyResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

/**
 * 消费端拦截器
 */
@Activate(
        group = {CommonConstants.CONSUMER},
        order = 10
)
/**
 * ConsumerExceptionFilter
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class ConsumerExceptionFilter implements Filter {

    public ConsumerExceptionFilter() {
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String i18n1 = invocation.getAttachment("i18n");
        Locale locale1 = null;
        if (StrUtil.isNotBlank(i18n1)) {
            String[] split = i18n1.split("_");
            locale1 = new Locale(split[0], split[1]);
        } else {
            locale1 = LocaleContextHolder.getLocale();
        }
        invocation.setAttachment("i18n", locale1.toString());
        Result result = invoker.invoke(invocation);
        RpcException rpcException;
        Object value = result.getValue();
        // 调用返回值
        if (value != null) {
            // fastJson 会序列化为字符串
            if (value instanceof String) {
                String string = value.toString();
                EasyResult easy4jResult = null;
                try {
                    easy4jResult = JSON.parseObject(string, EasyResult.class);
                } catch (Exception e) {
                }
                if (Objects.nonNull(easy4jResult)) {
                    if (!easy4jResult.isSuccess()) {
                        String code1 = easy4jResult.getCode();
                        String rpcMethod = easy4jResult.getRpcMethod();
                        String message = rpcMethod + "#" + easy4jResult.getMessage() + "#" + StrUtil.blankToDefault(easy4jResult.getErrorInfo(), "");
                        log.error("远程调用异常[" + code1 + ":" + message + "]：service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName());
                        rpcException = new RpcException(3, message);
                        throw rpcException;
                    }
                }
            } else {
                if (value instanceof EasyResult && GenericService.class != invoker.getInterface() && !this.isInjvmRefer(invoker.getUrl())) {
                    EasyResult easy4jResult = (EasyResult) value;
                    if (!easy4jResult.isSuccess()) {
                        String code1 = easy4jResult.getCode();
                        String rpcMethod = easy4jResult.getRpcMethod();
                        String message = rpcMethod + "#" + easy4jResult.getMessage() + "#" + StrUtil.blankToDefault(easy4jResult.getErrorInfo(), "");
                        log.error("远程调用异常[" + code1 + ":" + message + "]：service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName());
                        rpcException = new RpcException(3, message);
                        throw rpcException;
                    }
                }
            }
        }
        return result;
    }

    private boolean isInjvmRefer(URL url) {
        String scope = url.getParameter("scope");
        return "local".equals(scope) || url.getParameter("injvm", false);
    }

}
