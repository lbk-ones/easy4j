package easy4j.infra.log;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.webmvc.AbstractEasy4JWebMvcHandler;
import io.github.lbkones.pure.ReplacedBodyRequestWrapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class LogInterceptor extends AbstractEasy4JWebMvcHandler {
    @Override
    public Integer getOrder() {
        return super.getOrder();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(RequestLog.class)) {
            if (!Easy4j.getProperty(SysConstant.EASY4J_ENABLE_DB_REQUEST_LOG, boolean.class)) {
                return true;
            }
            RequestLog annotation = method.getAnnotation(RequestLog.class);
            String tag = annotation.tag();
            String tagDesc = annotation.tagDesc();
            String requestBody = "";
            if (request instanceof HttpServletRequestWrapper request1)  {
                ServletRequest request2 = request1.getRequest();
                if (request2 instanceof ReplacedBodyRequestWrapper request3) {
                    requestBody = request3.getOriginBody();
                }else{
                    try {
                        requestBody = ReplacedBodyRequestWrapper.readRequestBody(request);
                    } catch (IOException ignored) {
                    }
                }
            } else {
                Map<String, String[]> parameterMap = request.getParameterMap();
                requestBody = JacksonUtil.toJson(parameterMap);
            }
            if (StrUtil.isBlank(tag)) {
                tag = request.getRequestURI();
            }
            if (StrUtil.isBlank(tagDesc)) {
                tagDesc = method.getDeclaringClass().getName() + "#" + method.getName();
            }
            DbLog.beginLog(tag, tagDesc, JacksonUtil.compress(requestBody));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handlerMethod) {
        try {
            DbLog.endLog(ex);
        } finally {
            DbLog.removeThread();
        }
    }

}
