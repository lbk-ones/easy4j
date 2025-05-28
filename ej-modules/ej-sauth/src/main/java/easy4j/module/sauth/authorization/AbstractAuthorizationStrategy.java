package easy4j.module.sauth.authorization;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.sauth.domain.SecurityAuthority;
import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

public abstract class AbstractAuthorizationStrategy implements AuthorizationStrategy {


    @Override
    public boolean checkMethod(HandlerMethod handlerMethod) {
        return false;
    }

    @Override
    public boolean isNeedAuthentication(SecurityUserInfo userInfo, Set<SecurityAuthority> authorities, HttpServerRequest request, HttpServerResponse response) {
        return false;
    }

    @Override
    public boolean checkUri(HandlerMethod handlerMethod) {
        return false;
    }

    @Override
    public boolean needTakeToken(HandlerMethod handlerMethod, HttpServletRequest httpServerRequest, HttpServletResponse httpServerResponse) {
        String header = httpServerRequest.getHeader(SysConstant.EASY4J_NO_NEED_TOKEN);
        return !StrUtil.equals(header, "1");
    }

    @Override
    public boolean checkByUserInfo(SecurityUserInfo securityUserInfo) {
        return false;
    }
}
