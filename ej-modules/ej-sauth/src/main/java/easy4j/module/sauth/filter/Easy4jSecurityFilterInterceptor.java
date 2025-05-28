package easy4j.module.sauth.filter;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.utils.BusCode;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.sauth.annotations.OpenApi;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 获取用户信息
 */
public class Easy4jSecurityFilterInterceptor implements HandlerInterceptor {

    SessionStrategy sessionStrategy;


    SecurityContext securityContext;

    SecurityAuthorization authorizationStrategy;


    SecurityAuthentication securityAuthentication;

    public Easy4jSecurityFilterInterceptor(SessionStrategy sessionStrategy, SecurityContext securityContext, SecurityAuthorization authorizationStrategy, SecurityAuthentication securityAuthentication) {
        this.sessionStrategy = sessionStrategy;
        this.securityContext = securityContext;
        this.authorizationStrategy = authorizationStrategy;
        this.securityAuthentication = securityAuthentication;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handler1 = (HandlerMethod) handler;
            Method method = handler1.getMethod();
            // 开放api授权
            if (method.isAnnotationPresent(OpenApi.class)) {
                OpenApi annotation = method.getAnnotation(OpenApi.class);
                String xApiKey = request.getHeader(SysConstant.X_API_KEY);
                // TODO  api key
            } else {
                // take session
                String token = request.getHeader(SysConstant.X_ACCESS_TOKEN);

                boolean b1 = authorizationStrategy.needTakeToken(handler1, request, response);
                SecurityUserInfo securityUserInfo = null;
                if (b1) {
                    if (StrUtil.isBlank(token)) {
                        throw new EasyException(BusCode.A00029 + "," + SysConstant.X_ACCESS_TOKEN);
                    }
                    securityUserInfo = securityAuthentication.tokenAuthentication(token);
                    if (
                            StrUtil.isNotBlank(securityUserInfo.getErrorCode())
                    ) {
                        throw new EasyException(securityUserInfo.getErrorCode());
                    }
                } else {
                    // 如果不需要token鉴权 改用
                }
                authorizationStrategy.checkMethod(handler1);
                if (StrUtil.isBlank(token)) {
                    return true;
                }
            }


        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
