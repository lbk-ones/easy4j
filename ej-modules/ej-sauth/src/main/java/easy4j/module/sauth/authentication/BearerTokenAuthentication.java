package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.session.SessionStrategy;
import javax.servlet.http.HttpServletRequest;

/**
 * BearerToken认证方式 token是未知类型的TOKEn 这里不做具体鉴权实现由外部传入
 * 必须在认证入参对象传入 实现了IBearerAuthentication接口的对象，用来自定义鉴权
 *
 * @author bokun.li
 * @date 2025/11/14
 * @see LoadAuthentication
 */
public class BearerTokenAuthentication extends UserNamePasswordAuthentication {


    @Override
    public String getName() {
        return AuthenticationType.BearerToken.name();
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        IBearerAuthentication loadAuthentication = reqUser.getBearerAuthentication();
        if (null == loadAuthentication) {
            context.setErrorCode(BusCode.A00062);
            return null;
        }
        String bearerToken = reqUser.getShaToken();
        if (StrUtil.isBlank(bearerToken)) {
            context.setErrorCode(BusCode.A00064);
            return null;
        }
        if(!bearerToken.startsWith("Bearer ")){
            context.setErrorCode(BusCode.A00034);
            return null;
        }
        String subBearerToken = parseBearerToken(bearerToken);
        ISecurityEasy4jUser userBy = loadAuthentication.obtainUserInfo(subBearerToken);
        context.setDbUser(userBy);
        if (checkUserIsNotEnable(userBy,context)) {
            return null;
        }
        String errorCode = userBy.getErrorCode();
        if(StrUtil.isNotBlank(errorCode)){
            context.setErrorCode(errorCode);
            return null;
        }
        String username = userBy.getUsername();
        if (StrUtil.isBlank(username)) {
            context.setErrorCode(BusCode.A00063);
            return null;
        }
        syncReqUser(context, userBy);
        SessionStrategy sessionStrategy = getSessionStrategy();
        ISecurityEasy4jSession session = sessionStrategy.getSessionByUserName(username);
        context.setDbSession(session);
        return userBy;
    }

    private String parseBearerToken(String bearerToken) {
        return bearerToken.substring("Bearer ".length());
    }

    @Override
    public ISecurityEasy4jSession querySession(AuthenticationContext context) {
        // do nothing
        return context.getDbSession();
    }

    @Override
    public void verifyPre(ISecurityEasy4jUser user) {
        if (null == user) {
            user = new SecurityUser();
            user.setErrorCode(BusCode.A00004 + ",user");
            return;
        }
        HttpServletRequest servletRequest = getServletRequest();
        String method = servletRequest.getMethod();
        if (!"post".equalsIgnoreCase(method)) {
            user.setErrorCode(BusCode.A00030);
        }
    }

    @Override
    public void verify(AuthenticationContext context) {
        if (checkRepeatSession(context)) {
            return;
        }
        ISecurityEasy4jUser reqUser = context.getReqUser();
        IBearerAuthentication loadAuthentication = reqUser.getBearerAuthentication();
        String subBearerToken = parseBearerToken(reqUser.getShaToken());
        if (!loadAuthentication.verify(subBearerToken)) {
            String errorCode = reqUser.getErrorCode();
            if (StrUtil.isNotBlank(errorCode)) {
                context.setErrorCode(errorCode);
            } else {
                context.setErrorCode(BusCode.A00033);
            }
        }
    }
}
