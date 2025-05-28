package easy4j.module.sauth.authorization;

import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import easy4j.module.sauth.domain.SecurityAuthority;
import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

public class DefaultAuthorizationStrategy implements AuthorizationStrategy {

    @Override
    public Set<SecurityAuthority> getAuthorizationByUsername(String userName) {
        return null;
    }

    @Override
    public boolean checkMethod(HandlerMethod handlerMethod) {
        return false;
    }

    @Override
    public boolean checkUri(HandlerMethod handlerMethod) {
        return false;
    }

    @Override
    public boolean needTakeToken(HandlerMethod handlerMethod, HttpServletRequest httpServerRequest, HttpServletResponse httpServerResponse) {
        return false;
    }

    @Override
    public boolean checkByUserInfo(SecurityUserInfo securityUserInfo) {
        return false;
    }

    @Override
    public boolean isNeedAuthentication(SecurityUserInfo userInfo, Set<SecurityAuthority> authorities, HttpServerRequest request, HttpServerResponse response) {
        return false;
    }
}
