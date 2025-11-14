package easy4j.module.sauth.authentication;


import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;

/**
 * Bearer Token 方式 但是token是明确的JwtToken
 * Bearer JwtToken 认证方式 用于拦截器 和登录 两者并用
 * 如果是登录认证那么还是要传入用户名和密码相关
 * 如果是拦截器那么会从jwt里面解析出用户名，再反查用户信息
 *
 * @author bokun.li
 * @date 2025-11-14
 */
public class BearerJwtTokenAuthAuthentication extends JwtAuthAuthentication {


    @Override
    public String getName() {
        return AuthenticationType.BearerJwtToken.name();
    }

    @Override
    public String getJwtToken(AuthenticationContext context){
        ISecurityEasy4jUser reqUser = context.getReqUser();
        return parseBearerToken(reqUser.getShaToken(),context);
    }

    private String parseBearerToken(String bearerToken,AuthenticationContext context) {
        if (StrUtil.isBlank(bearerToken)) {
            context.setErrorCode(BusCode.A00064);
            return null;
        }
        if(!bearerToken.startsWith("Bearer ")){
            context.setErrorCode(BusCode.A00034);
            return null;
        }
        return bearerToken.substring("Bearer ".length());
    }

}
