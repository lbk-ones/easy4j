package easy4j.module.sauth.filter;

import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.api.user.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

/**
 * 当关闭sauth模块之后，从网关获取用户信息
 *
 * @author bokun.li
 */
public interface IGatewayUserContext {

    UserContext handlerFilter(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler);

    default String getUserContextName(){
        return SysConstant.SESSION_USER;
    }

}
