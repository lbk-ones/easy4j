package easy4j.module.sauth.authorization;

import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import easy4j.module.sauth.domain.SecurityAuthority;
import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

public interface AuthorizationStrategy {

    /**
     * 根据方法来拦截是否该过
     *
     * @param handlerMethod
     * @return
     */
    boolean checkMethod(HandlerMethod handlerMethod);

    /**
     * 根究url来检查
     *
     * @param handlerMethod
     * @return
     */
    boolean checkUri(HandlerMethod handlerMethod);


    /**
     * 是否应该携带token
     *
     * @param handlerMethod
     * @param httpServerRequest
     * @param httpServerResponse
     * @return
     */
    boolean needTakeToken(HandlerMethod handlerMethod, HttpServletRequest httpServerRequest, HttpServletResponse httpServerResponse);

    /**
     * 根据用户信息来过滤
     *
     * @param securityUserInfo
     * @return
     */
    boolean checkByUserInfo(SecurityUserInfo securityUserInfo);

    /**
     * 是否需要鉴权
     *
     * @return
     */
    boolean isNeedAuthentication(SecurityUserInfo userInfo, Set<SecurityAuthority> authorities, HttpServerRequest request, HttpServerResponse response);

    /**
     * 根据用户名查询权限
     *
     * @param userName
     * @return
     */
    Set<SecurityAuthority> getAuthorizationByUsername(String userName);
}
