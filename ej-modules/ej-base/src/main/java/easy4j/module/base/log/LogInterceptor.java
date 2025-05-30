package easy4j.module.base.log;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.BusCode;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.json.JacksonUtil;
import easy4j.module.base.web.AbstractEasy4JWebMvcHandler;
import easy4j.module.base.web.filter.RepeatServletRequestWrapper;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            if (request instanceof RepeatServletRequestWrapper) {
                RepeatServletRequestWrapper request1 = (RepeatServletRequestWrapper) request;
                requestBody = request1.getRequestBody();
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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handlerMethod) {
        try {
            DbLog.endLog(ex);
        } finally {
            DbLog.removeThread();
        }
    }
}
