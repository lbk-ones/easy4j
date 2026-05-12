package easy4j.infra.webmvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class WebContextUtil {

    /**
     * 获取当前 HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return ((ServletRequestAttributes) attrs).getRequest();
    }

    /**
     * 获取当前 HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return ((ServletRequestAttributes) attrs).getResponse();
    }
}