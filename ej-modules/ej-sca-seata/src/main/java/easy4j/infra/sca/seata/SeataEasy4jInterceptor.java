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
package easy4j.infra.sca.seata;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.webmvc.AbstractEasy4JWebMvcHandler;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

import static easy4j.infra.webmvc.PerRequestInterceptor.REQUEST_IP_ADDR;

/**
 * SeataEasy4jInterceptor
 * print xid to lookup tx
 *
 * @author bokun.li
 * @date 2025/6/25
 */
@Slf4j
public class SeataEasy4jInterceptor extends AbstractEasy4JWebMvcHandler {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        boolean enableSeata = Easy4j.getProperty(SysConstant.EASY4J_SEATA_ENABLE, boolean.class);
        if (method.isAnnotationPresent(GlobalTransactional.class) && enableSeata) {
            // 可能拿不到这个 但是不重要 日志中会集中打印
            String branchId = MDC.get(RootContext.MDC_KEY_BRANCH_ID);
            String ip = String.valueOf(request.getAttribute(REQUEST_IP_ADDR));
            String methodType = request.getMethod();
            String currentXid = SeataUtils.getCurrentXid();
            if (log.isInfoEnabled()) {
                log.info(SysLog.compact("seata -> ip:{},methodType:{},method:{},xid:{},branchId:{}", ip, methodType, method.getName(), currentXid, branchId));
            }
        }
        return true;
    }
}
