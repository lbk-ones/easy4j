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
package easy4j.module.base.web;

import easy4j.module.base.context.Easy4jContext;
import easy4j.module.base.context.Easy4jContextFactory;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SysConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 初始化上下文
 * 开启简单链路最终
 * 将链路ID放到上下文去
 */
@Slf4j
@Component
public class PerRequestInterceptor implements HandlerInterceptor {
    public static final String INTERCEPTOR_MARK = SysConstant.PARAM_PREFIX.toLowerCase() + "_request_lifecycle_intercepted";
    public static final String START_TIME_KEY = SysConstant.PARAM_PREFIX.toLowerCase() + "_request_start_time";
    public static final String REQUEST_ID_KEY = SysConstant.PARAM_PREFIX.toLowerCase() + "_request_id";
    public static final String REQUEST_PRINT_LOG = SysConstant.PARAM_PREFIX.toLowerCase() + "_is_print_log";
    public static final String REQUEST_IP_ADDR = SysConstant.PARAM_PREFIX.toLowerCase() + "_ip_addr";

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
        try {
            setAttribute(request, response);
            String ipAddr = IpUtils.getIpAddr(request);
            request.setAttribute(REQUEST_IP_ADDR, ipAddr);
            Object attribute = request.getAttribute(REQUEST_PRINT_LOG);
            if (null != attribute && (boolean) attribute) {
                // 打印控制器处理完成日志（视图未渲染）
                log.info("[请求开始] ip: {}, 路径: {}, 方法:{}", ipAddr, request.getRequestURI(), request.getMethod());
            }
        } catch (Exception e) {
            log.error(this.getClass().getName() + "--preHandler find a exception", e);
        }

        return true;
    }

    private static void setAttribute(HttpServletRequest request, HttpServletResponse response) {
        // 标记已拦截
        request.setAttribute(INTERCEPTOR_MARK, true);
        // 生成唯一请求ID（用于日志追踪）
        String requestId = UUID.randomUUID().toString().replaceAll("-", "");
        request.setAttribute(REQUEST_ID_KEY, requestId);
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_KEY, startTime);
        boolean isPrintLog = Easy4j.getProperty(SysConstant.EASY4J_PRINT_REQUEST_LOG, boolean.class);
        request.setAttribute(REQUEST_PRINT_LOG, isPrintLog);
        response.setHeader(SysConstant.TRACE_ID_NAME, requestId);
        MDC.put(SysConstant.TRACE_ID_NAME, "[" + requestId + "]");
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(SysConstant.TRACE_ID_NAME, SysConstant.TRACE_ID_NAME, requestId);
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
                           org.springframework.web.servlet.ModelAndView modelAndView) {
        // 仅处理首次拦截的请求
        if (request.getAttribute(INTERCEPTOR_MARK) == null) return;
        Object attribute = request.getAttribute(REQUEST_PRINT_LOG);
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
