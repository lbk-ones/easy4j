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

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import easy4j.infra.webmvc.IpUtils;


import javax.servlet.http.HttpServletRequest;

/**
 * 【示例】sentinel ip和参数授权规则拦截器(黑名单白名单)
 * 1. 有参数origin的时候走参数拦截规则
 * 2. 当参数为空时走ip拦截模式
 */
public class DefaultRequestOriginParser implements RequestOriginParser {
    @Override
    public String parseOrigin(HttpServletRequest request) {
        //基于请求参数,origin对应授权规则中的流控应用名称,也可通过getHeader传参
        String origin = request.getParameter("origin");
        if (StringUtils.isNotEmpty(origin)) {
            return origin;
        } else {
            //当参数为空使用ip拦截模式
            return IpUtils.getIpAddr(request);
        }
    }
}
