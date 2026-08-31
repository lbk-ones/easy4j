package easy4j.infra.webmvc;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.context.api.req.BaseReq;
import easy4j.infra.context.api.user.UserContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import java.lang.reflect.Type;

public class CommonRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        if(body instanceof BaseReq baseReq){
            UserContext userContext = Easy4j.getUserContext();
            baseReq.setUserInfo(userContext);
        }

        return body;
    }
}
