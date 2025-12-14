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
/*
 * Copyright 2016-2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package easy4j.module.jaeger.opentracing.web;

import easy4j.infra.common.utils.ListTs;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TracingFilter
 *
 * @author bokun.li
 * @date 2025-05
 */
@WebFilter(urlPatterns = "/*", filterName = "easy4jJaegerWebFilter")
public class TracingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(TracingFilter.class);

    /**
     * Use as a key of {@link ServletContext#setAttribute(String, Object)} to set span decorators
     */
    public static final String SPAN_DECORATORS = TracingFilter.class.getName() + ".spanDecorators";
    /**
     * Use as a key of {@link ServletContext#setAttribute(String, Object)} to skip pattern
     */
    public static final String SKIP_PATTERN = TracingFilter.class.getName() + ".skipPattern";

    /**
     * Used as a key of {@link HttpServletRequest#setAttribute(String, Object)} to inject server span context
     */
    public static final String SERVER_SPAN_CONTEXT = TracingFilter.class.getName() + ".activeSpanContext";

    private FilterConfig filterConfig;

    protected Tracer tracer;
    private List<ServletFilterSpanDecorator> spanDecorators;
    private Pattern skipPattern;

    /**
     * Tracer instance has to be registered with {@link GlobalTracer#register(Tracer)}.
     */
    public TracingFilter() {
        this(GlobalTracer.get());
    }

    /**
     * @param tracer
     */
    public TracingFilter(Tracer tracer) {
        this(tracer, Collections.singletonList(ServletFilterSpanDecorator.STANDARD_TAGS), null);
    }

    /**
     * @param tracer         tracer
     * @param spanDecorators decorators
     * @param skipPattern    null or pattern to exclude certain paths from tracing e.g. "/health"
     */
    public TracingFilter(Tracer tracer, List<ServletFilterSpanDecorator> spanDecorators, Pattern skipPattern) {
        this.tracer = tracer;
        this.spanDecorators = new ArrayList<>(spanDecorators);
        this.spanDecorators.removeAll(Collections.singleton(null));
        this.skipPattern = skipPattern;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        ServletContext servletContext = filterConfig.getServletContext();

        Object tracerObj = servletContext.getAttribute(Tracer.class.getName());
        if (tracerObj instanceof Tracer) {
            tracer = (Tracer) tracerObj;
        } else {
            servletContext.setAttribute(Tracer.class.getName(), tracer);
        }

        // use decorators from context attributes
        Object contextAttribute = servletContext.getAttribute(SPAN_DECORATORS);
        if (contextAttribute instanceof Collection) {
            List<ServletFilterSpanDecorator> decorators = new ArrayList<>();
            for (Object decorator : (Collection) contextAttribute) {
                if (decorator instanceof ServletFilterSpanDecorator) {
                    decorators.add((ServletFilterSpanDecorator) decorator);
                } else {
                    log.warn(decorator + " is not an instance of " + ServletFilterSpanDecorator.class);
                }
            }
            this.spanDecorators = !decorators.isEmpty() ? decorators : this.spanDecorators;
        }

        contextAttribute = servletContext.getAttribute(SKIP_PATTERN);
        if (contextAttribute instanceof Pattern) {
            skipPattern = (Pattern) contextAttribute;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        if (!isTraced(httpRequest, httpResponse)) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        /**
         * 如果请求已经被trace了 那么 则不需要再次创建span
         */
        if (servletRequest.getAttribute(SERVER_SPAN_CONTEXT) != null) {
            chain.doFilter(servletRequest, servletResponse);
        } else {
            SpanContext extractedContext = tracer.extract(Format.Builtin.HTTP_HEADERS,
                    new HttpServletRequestExtractAdapter(httpRequest));
            final Span span = tracer.buildSpan(httpRequest.getRequestURI())
                    .asChildOf(extractedContext)
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                    .start();

            httpRequest.setAttribute(SERVER_SPAN_CONTEXT, span.context());

            for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                spanDecorator.onRequest(httpRequest, span);
            }

            try (Scope scope = tracer.activateSpan(span)) {
                chain.doFilter(servletRequest, servletResponse);
                if (!httpRequest.isAsyncStarted()) {
                    for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                        spanDecorator.onResponse(httpRequest, httpResponse, span);
                    }
                }
                // catch all exceptions (e.g. RuntimeException, ServletException...)
            } catch (Throwable ex) {
                for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                    spanDecorator.onError(httpRequest, httpResponse, ex, span);
                }
                throw ex;
            } finally {
                if (httpRequest.isAsyncStarted()) {
                    // 如果async已经完成了怎么办？这不会被调用
                    httpRequest.getAsyncContext()
                            .addListener(new AsyncListener() {
                                @Override
                                public void onComplete(AsyncEvent event) throws IOException {
                                    HttpServletRequest httpRequest = (HttpServletRequest) event.getSuppliedRequest();
                                    HttpServletResponse httpResponse = (HttpServletResponse) event.getSuppliedResponse();
                                    for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                                        spanDecorator.onResponse(httpRequest,
                                                httpResponse,
                                                span);
                                    }
                                    span.finish();
                                }

                                @Override
                                public void onTimeout(AsyncEvent event) throws IOException {
                                    HttpServletRequest httpRequest = (HttpServletRequest) event.getSuppliedRequest();
                                    HttpServletResponse httpResponse = (HttpServletResponse) event.getSuppliedResponse();
                                    for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                                        spanDecorator.onTimeout(httpRequest,
                                                httpResponse,
                                                event.getAsyncContext().getTimeout(),
                                                span);
                                    }
                                }

                                @Override
                                public void onError(AsyncEvent event) throws IOException {
                                    HttpServletRequest httpRequest = (HttpServletRequest) event.getSuppliedRequest();
                                    HttpServletResponse httpResponse = (HttpServletResponse) event.getSuppliedResponse();
                                    for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                                        spanDecorator.onError(httpRequest,
                                                httpResponse,
                                                event.getThrowable(),
                                                span);
                                    }
                                }

                                @Override
                                public void onStartAsync(AsyncEvent event) throws IOException {
                                }
                            });
                } else {
                    // If not async, then need to explicitly finish the span associated with the scope.
                    // This is necessary, as we don't know whether this request is being handled
                    // asynchronously until after the scope has already been started.
                    span.finish();
                }
            }
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    /**
     * It checks whether a request should be traced or not.
     *
     * @param httpServletRequest  request
     * @param httpServletResponse response
     * @return whether request should be traced or not
     */
    protected boolean isTraced(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // skip URLs matching skip pattern
        // e.g. pattern is defined as '/health|/status' then URL 'http://localhost:5000/context/health' won't be traced
        if (skipPattern != null) {
            int contextLength = httpServletRequest.getContextPath() == null ? 0 : httpServletRequest.getContextPath().length();
            String url = httpServletRequest.getRequestURI().substring(contextLength);
            return !skipPattern.matcher(url).matches();
        }
        String url = httpServletRequest.getRequestURI();
        List<String> list = ListTs.asList(".css", ".js", ".jpg", ".gif", ".png", ".svg", ".ico");
        if (list.stream().anyMatch(url::endsWith)) {
            return false;
        }
        return true;
    }

    /**
     * Get context of server span.
     *
     * @param servletRequest request
     * @return server span context
     */
    public static SpanContext serverSpanContext(ServletRequest servletRequest) {
        return (SpanContext) servletRequest.getAttribute(SERVER_SPAN_CONTEXT);
    }
}
