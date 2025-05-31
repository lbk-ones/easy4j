package easy4j.module.base.log;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.BusCode;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.json.JacksonUtil;
import easy4j.module.base.web.AbstractEasy4JWebMvcHandler;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Map;

public class LogInterceptor extends AbstractEasy4JWebMvcHandler {

    @Override
    public void preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(RequestLog.class)) {
            if (!Easy4j.getProperty(SysConstant.EASY4J_ENABLE_DB_REQUEST_LOG, boolean.class)) {
                return;
            }
            RequestLog annotation = method.getAnnotation(RequestLog.class);
            String tag = annotation.tag();
            String tagDesc = annotation.tagDesc();
            String requestBody = "";
            if (request instanceof ContentCachingRequestWrapper) {
                requestBody = getRequestBody((ContentCachingRequestWrapper) request);
                String contentType = request.getContentType();
                if (StrUtil.startWithAnyIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE)) {
                    if (!JacksonUtil.isValidJson(requestBody)) {
                        throw new EasyException(BusCode.A00040);
                    }
                }

            } else {
                Map<String, String[]> parameterMap = request.getParameterMap();
                requestBody = JacksonUtil.toJson(parameterMap);
            }
            DbLog.beginLog(tag, tagDesc, requestBody);

        }
    }

    private static String getRequestBody(ContentCachingRequestWrapper request) {
        String requestBody;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ServletInputStream input = request.getInputStream()) {
                byte[] buffer = new byte[1024 * 8];
                int len;
                while ((len = input.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
            }
            requestBody = baos.toString(request.getCharacterEncoding());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return requestBody;
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
