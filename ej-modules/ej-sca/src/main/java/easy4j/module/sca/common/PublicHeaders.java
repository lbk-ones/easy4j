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
package easy4j.module.sca.common;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

/**
 * PublicHeaders
 *
 * @author bokun.li
 * @date 2025/6/19
 */
public class PublicHeaders {

    /**
     * rpc 公共请求头悬挂
     *
     * @author bokun.li
     * @date 2025/6/19
     */
    public static void initHeader(Object object2) {
        // trace id
        Easy4jContext context = Easy4j.getContext();
        Optional<Object> threadHashValue = context.getThreadHashValue(SysConstant.TRACE_ID_NAME, SysConstant.TRACE_ID_NAME);
        // sha token
        Optional<Object> xAccessToken = context.getThreadHashValue(SysConstant.X_ACCESS_TOKEN, SysConstant.X_ACCESS_TOKEN);
        // tenantId
        Optional<Object> xTenantId = context.getThreadHashValue(SysConstant.X_TENANT_ID, SysConstant.X_TENANT_ID);
        // this value must be the same for the same rpc request
        // easy4j trace id
        Optional<Object> easy4jRpcTrace = context.getThreadHashValue(SysConstant.EASY4J_RPC_TRACE, SysConstant.EASY4J_RPC_TRACE);
        if (object2 instanceof HttpHeaders) {
            HttpHeaders object21 = (HttpHeaders) object2;
            threadHashValue.ifPresent(object -> object21.set(SysConstant.SERVER_TRACE_NAME, object.toString()));
            xAccessToken.ifPresent(object -> object21.set(SysConstant.X_ACCESS_TOKEN, object.toString()));
            xTenantId.ifPresent(object -> object21.set(SysConstant.X_TENANT_ID, object.toString()));
            easy4jRpcTrace.ifPresent(object -> object21.set(SysConstant.EASY4J_RPC_TRACE, object.toString()));
        } else if (object2 instanceof RequestTemplate) {
            RequestTemplate requestTemplate = (RequestTemplate) object2;
            threadHashValue.ifPresent(object -> requestTemplate.header(SysConstant.SERVER_TRACE_NAME, object.toString()));
            xAccessToken.ifPresent(object -> requestTemplate.header(SysConstant.X_ACCESS_TOKEN, object.toString()));
            xTenantId.ifPresent(object -> requestTemplate.header(SysConstant.X_TENANT_ID, object.toString()));
            easy4jRpcTrace.ifPresent(object -> requestTemplate.header(SysConstant.EASY4J_RPC_TRACE, object.toString()));
        }
    }

}
