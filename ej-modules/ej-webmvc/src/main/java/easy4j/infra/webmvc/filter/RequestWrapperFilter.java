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
package easy4j.infra.webmvc.filter;

import easy4j.infra.common.utils.SysConstant;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;


/**
 * 过滤器
 * 早于 springmvc 拦截器
 *
 * @author bokun.li
 */
//@WebFilter(urlPatterns = "/*")
public class RequestWrapperFilter implements Filter {
    private static Integer LENGTH = 1024 * 1024 * 10;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        FilterRegistrationBean<RequestWrapperFilter> bean = new FilterRegistrationBean<>();
        Map<String, String> initParameters = bean.getInitParameters();
        String s = initParameters.get(SysConstant.EASY4J_CACHE_CONTENT_LENGTH);

        try {
            LENGTH = Integer.parseInt(s);
        } catch (Exception ignored) {
        }

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest request1 = (HttpServletRequest) request;
            if (hasRequestBody(request1)) {
                // 包装请求，缓存请求体
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request, LENGTH);
                chain.doFilter(wrappedRequest, response);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public static boolean hasRequestBody(HttpServletRequest request) {
        // 1. 检查请求方法是否可能有请求体
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            return false;
        }

        // 2. 检查 Content-Length
        long contentLength = request.getContentLengthLong();
        if (contentLength > 0) {
            return true;
        }

        // 3. 检查 Transfer-Encoding
        String transferEncoding = request.getHeader("Transfer-Encoding");
        if (transferEncoding != null && !transferEncoding.isEmpty()) {
            return true;
        }

        // 4. 检查 Content-Type（某些类型可能隐含存在请求体）
        String contentType = request.getContentType();
        if (contentType != null && !contentType.isEmpty()) {
            // 常见的有请求体的 Content-Type
            return contentType.startsWith("application/json") ||
                    contentType.startsWith("application/xml") ||
                    contentType.startsWith("text/plain") ||
                    contentType.startsWith("multipart/form-data");
        }

        return false;
    }
}