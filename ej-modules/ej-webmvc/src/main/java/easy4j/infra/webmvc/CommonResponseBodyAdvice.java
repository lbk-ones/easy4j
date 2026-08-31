package easy4j.infra.webmvc;

import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


public class CommonResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {


        if(body instanceof EasyResult<?> ex){
            // 自动回写 traceId
            String traceId = Easy4jContextFactory.getContext()
                    .getThreadHashValue(SysConstant.TRACE_ID_NAME, SysConstant.TRACE_ID_NAME)
                    .map(Object::toString)
                    .orElse("");
            ex.setTraceId(traceId);

            // 自动回写耗时
            if (request instanceof HttpServletRequest httpServletRequest) {
                Object attribute = httpServletRequest.getAttribute(PerRequestInterceptor.START_TIME_KEY);
                if(attribute!=null){
                    long l = Long.parseLong(attribute.toString());
                    long l1 = System.currentTimeMillis();
                    ex.setCost(l1 - l);
                }
            }

        }
        return body;
    }
}
