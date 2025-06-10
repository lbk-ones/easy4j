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
package easy4j.infra.webmvc;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.ServiceLoaderUtils;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.Easy4jContextFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 初始化上下文
 * 开启简单链路最终
 * 将链路ID放到上下文去
 * 这里面不处理异常 出了异常就抛出去
 *
 * @author bokun.li
 */
@Slf4j
//@Component
public class PerRequestInterceptor implements HandlerInterceptor {
    public static final String PRE_HANDLER = "preHandle";
    public static final String POST_HANDLE = "postHandle";

    public static final String AFTER_COMPLETION = "afterCompletion";
    public static final String INTERCEPTOR_MARK = SysConstant.PARAM_PREFIX.toLowerCase() + "_request_lifecycle_intercepted";
    public static final String START_TIME_KEY = SysConstant.PARAM_PREFIX.toLowerCase() + "_request_start_time";
    public static final String REQUEST_ID_KEY = SysConstant.PARAM_PREFIX.toLowerCase() + "_request_id";
    public static final String REQUEST_PRINT_LOG = SysConstant.PARAM_PREFIX.toLowerCase() + "_is_print_log";
    public static final String REQUEST_IP_ADDR = SysConstant.PARAM_PREFIX.toLowerCase() + "_ip_addr";
    private final List<Easy4JWebMvcHandler> easy4JWebMvcHandlers = ServiceLoaderUtils.load(Easy4JWebMvcHandler.class).stream().sorted(Comparator.comparing(Easy4JWebMvcHandler::getOrder)).collect(Collectors.toList());


    /**
     * 请求进入时触发，记录开始时间、生成唯一请求 ID，打印初始日志。
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 检查是否已拦截（防止转发/包含重复执行）
        if (request.getAttribute(INTERCEPTOR_MARK) != null) {
            return true;
        }
        setAttribute(request, response);
        String ipAddr = IpUtils.getIpAddr(request);
        request.setAttribute(REQUEST_IP_ADDR, ipAddr);
        Object attribute = request.getAttribute(REQUEST_PRINT_LOG);
        if (null != attribute && (boolean) attribute) {
            // 打印控制器处理完成日志（视图未渲染）
            log.info("[请求开始] ip: {}, 路径: {}, 方法:{}", ipAddr, request.getRequestURI(), request.getMethod());
        }
        try {
            return handlerMethods(request, response, handler, null, null, PRE_HANDLER);

        } catch (Exception e) {
            try {
                handlerMethods(request, response, handler, null, e, AFTER_COMPLETION);
            } catch (Exception e2) {
                throw e;
            }
            return false;
        }

    }

    // spi call
    private boolean handlerMethods(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView,
            Exception ex,
            String type
    ) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            for (Easy4JWebMvcHandler easy4JWebMvcHandler : easy4JWebMvcHandlers) {
                if (PRE_HANDLER.equals(type)) {
                    if (!easy4JWebMvcHandler.preHandle(request, response, handlerMethod)) {
                        return false;
                    }
                }
                if (POST_HANDLE.equals(type)) {
                    easy4JWebMvcHandler.postHandle(request, response, modelAndView, handlerMethod);
                }
                if (AFTER_COMPLETION.equals(type)) {
                    easy4JWebMvcHandler.afterCompletion(request, response, ex, handlerMethod);
                }
            }
        }
        return true;

    }

    private static void setAttribute(HttpServletRequest request, HttpServletResponse response) {
        String requestId = request.getHeader(SysConstant.SERVER_TRACE_NAME);
        String easy4jTraceId = request.getHeader(SysConstant.EASY4J_RPC_TRACE);

        // 标记已拦截
        request.setAttribute(INTERCEPTOR_MARK, true);
        if (StrUtil.isBlank(requestId)) {
            // 生成唯一请求ID（用于日志追踪）
            requestId = UUID.randomUUID().toString().replaceAll("-", "");
            request.setAttribute(REQUEST_ID_KEY, requestId);
            // 如果使用简单的分布式链路id那么这个值和SysConstant.TRACE_ID_NAME这个值一样
            if (StrUtil.isBlank(easy4jTraceId)) {
                easy4jTraceId = requestId;
            }
        }

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_KEY, startTime);
        boolean isPrintLog = Easy4j.getProperty(SysConstant.EASY4J_PRINT_REQUEST_LOG, boolean.class);
        request.setAttribute(REQUEST_PRINT_LOG, isPrintLog);
        // 多放一个用于向下传递  TRACE_ID_NAME 是其他标准的分布式链路ID (它可能并不能证明是这一次请求的传递) EASY4J_RPC_TRACE 给自己系统使用的

        if (StrUtil.isBlank(easy4jTraceId)) {
            easy4jTraceId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        request.setAttribute(SysConstant.EASY4J_RPC_TRACE, easy4jTraceId);

        response.setHeader(SysConstant.TRACE_ID_NAME, requestId);
        MDC.put(SysConstant.TRACE_ID_NAME, "[" + requestId + "]");
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(SysConstant.TRACE_ID_NAME, SysConstant.TRACE_ID_NAME, requestId);
        context.registerThreadHash(SysConstant.EASY4J_RPC_TRACE, SysConstant.EASY4J_RPC_TRACE, easy4jTraceId);
    }

    /**
     * 控制器方法执行完毕但视图未渲染时触发（适用于需要操作 ModelAndView 的场景）
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // 仅处理首次拦截的请求
        if (request.getAttribute(INTERCEPTOR_MARK) == null) return;
        Object attribute = request.getAttribute(REQUEST_PRINT_LOG);

        handlerMethods(request, response, handler, modelAndView, null, POST_HANDLE);

        if (null != attribute && (boolean) attribute) {
            // 打印控制器处理完成日志（视图未渲染）
            log.info("[控制器完成] 状态码: {}", response.getStatus());
        }


    }

    /**
     * 请求完全结束（视图渲染完成 / 异常抛出）时触发，计算总耗时并打印最终日志（包含异常信息）。
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        boolean isPrintLog = (boolean) request.getAttribute(REQUEST_PRINT_LOG);
        try {
            // 仅处理首次拦截的请求
            if (request.getAttribute(INTERCEPTOR_MARK) == null) return;

            // 计算请求耗时
            long startTime = (long) request.getAttribute(START_TIME_KEY);
            long duration = System.currentTimeMillis() - startTime;

            handlerMethods(request, response, handler, null, ex, AFTER_COMPLETION);

            if (isPrintLog) {
                // 打印请求完成日志（含异常信息）
                if (ex == null || ex instanceof EasyException) {
                    log.info("[请求完成] 耗时: {}ms, 状态码: {}", duration, response.getStatus());
                } else {
                    log.error("[请求异常] 耗时: {}ms, 异常: {}", duration, ex.getMessage());
                }
            } else {
                if (ex != null && !(ex instanceof EasyException)) {
                    Object ipAddr = request.getAttribute(REQUEST_IP_ADDR);
                    log.error("[请求异常] 耗时:{}ms, ip: {}, 路径: {}, 方法:{}, 异常: {}", duration, ipAddr, request.getRequestURI(), request.getMethod(), ex.getMessage());
                }
            }

        } finally {
            MDC.remove(SysConstant.TRACE_ID_NAME);
            Easy4jContextFactory.getContext().clearHash();
        }


    }
}    
