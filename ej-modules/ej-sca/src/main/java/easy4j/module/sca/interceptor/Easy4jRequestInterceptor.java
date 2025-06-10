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
package easy4j.module.sca.interceptor;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import easy4j.module.sca.util.HttpUtils;
import easy4j.module.sca.util.PathMatcherUtil;
import easy4j.module.sca.util.SignUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jodd.util.StringPool;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;

/**
 * Easy4jRequestInterceptor
 * 请求拦截器
 * 将链路id传递给服务端
 * 将链路id传递给服务端
 *
 * @author bokun.li
 * @date 2025-05-29 21:57:31
 */
public class Easy4jRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Easy4j.debug("feign client request url {}", template.url());

        // trace id
        Easy4jContext context = Easy4j.getContext();
        Optional<Object> threadHashValue = context.getThreadHashValue(SysConstant.TRACE_ID_NAME, SysConstant.TRACE_ID_NAME);
        threadHashValue.ifPresent(object -> template.header(SysConstant.SERVER_TRACE_NAME, object.toString()));

        // sha token
        Optional<Object> xAccessToken = context.getThreadHashValue(SysConstant.X_ACCESS_TOKEN, SysConstant.X_ACCESS_TOKEN);
        xAccessToken.ifPresent(object -> template.header(SysConstant.X_ACCESS_TOKEN, object.toString()));

        // tenantId
        Optional<Object> xTenantId = context.getThreadHashValue(SysConstant.X_TENANT_ID, SysConstant.X_TENANT_ID);
        xTenantId.ifPresent(object -> template.header(SysConstant.X_TENANT_ID, object.toString()));

        // this value must be the same for the same rpc request
        // easy4j trace id
        Optional<Object> easy4jRpcTrace = context.getThreadHashValue(SysConstant.EASY4J_RPC_TRACE, SysConstant.EASY4J_RPC_TRACE);
        easy4jRpcTrace.ifPresent(object -> template.header(SysConstant.EASY4J_RPC_TRACE, object.toString()));


        // sign url
        String ejSysPropertyName = Easy4j.getEjSysPropertyName(EjSysProperties::getSignUrls);
        List<String> signUrlsArray = StrUtil.split(Easy4j.getProperty(ejSysPropertyName), StringPool.COMMA);
        if (PathMatcherUtil.matches(signUrlsArray, template.path())) {
            try {
                Easy4j.debug("============================ [begin] fegin starter url ============================");
                Easy4j.debug(template.path());
                Easy4j.debug(template.method());
                String queryLine = template.queryLine();
                if (queryLine.startsWith("?")) {
                    queryLine = queryLine.substring(1);
                }
                Easy4j.debug(queryLine);
                if (template.body() != null) {
                    Easy4j.debug(new String(template.body()));
                }
                SortedMap<String, String> allParams = HttpUtils.getAllParams(template.path(), queryLine, template.body(), template.method());
                String sign = SignUtil.getParamsSign(allParams);
                Easy4j.info("feign request params sign: {}", sign);
                Easy4j.debug("============================ [end] fegin starter url ============================");
                template.header(SysConstant.X_SIGN, sign);
                template.header(SysConstant.X_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}