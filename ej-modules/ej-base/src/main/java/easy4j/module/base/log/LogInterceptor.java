package easy4j.module.base.log;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.BusCode;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.json.JacksonUtil;
import easy4j.module.base.web.AbstractEasy4JWebMvcHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            if (request instanceof ContentCachingRequestWrapper) {

                // 好烦呀 这里是不能提前拿值的。。可恶
                requestBody = getRequestBody((ContentCachingRequestWrapper) request);

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

    // 强制缓存
    private static void forceReadBody(ContentCachingRequestWrapper request) {
        //  还只能  getInputStream 不能 getReader 具有迷惑性 6
        try (ServletInputStream inputStream = request.getInputStream()) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            // 读取但不消费内容
            byte[] buffer = new byte[1024];
            while (bufferedInputStream.read(buffer) != -1) {
                // 不能删除
            }
        } catch (IOException e) {
            // 记录错误但不中断请求
            log.warn("Failed to read request body", e);
        }
    }

    private static String getRequestBody(ContentCachingRequestWrapper request) {
        // 只处理可能有请求体的 HTTP 方法
        request.getContentAsByteArray();
        byte[] content = request.getContentAsByteArray();

        // 使用请求编码或默认 UTF-8
        String charset = request.getCharacterEncoding();

        try {
            return new String(content, charset);
        } catch (UnsupportedEncodingException e) {
            // 回退到 UTF-8
            return new String(content, StandardCharsets.UTF_8);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handlerMethod) {
        try {
            String requestBody = "";
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper request1 = (ContentCachingRequestWrapper) request;
                if (request1.getContentAsByteArray().length == 0) {
                    forceReadBody(request1);
                }
                // 这里倒是可以拿但是不完美，不开心。。
                requestBody = getRequestBody(request1);
                String contentType = request.getContentType();
                if (StrUtil.startWithAnyIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE)) {
                    if (StrUtil.isNotBlank(requestBody) && !JacksonUtil.isValidJson(requestBody)) {
                        throw new EasyException(BusCode.A00040);
                    }
                }
            }
            final String requestBody_ = requestBody;
            if (StrUtil.isNotBlank(requestBody_)) {
                DbLog.set(e -> e.setParams(JacksonUtil.compress(requestBody_)));
            }
            DbLog.endLog(ex);
        } finally {
            DbLog.removeThread();
        }
    }

}
