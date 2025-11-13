package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.session.SessionStrategy;

import javax.servlet.http.HttpServletRequest;

/**
 * 其他认证方式，比如手机号什么的
 *
 * @author bokun.li
 * @date 2025/11/13
 */
public class OtherAuthentication extends UserNamePasswordAuthentication {


    @Override
    public String getName() {
        return AuthenticationType.Other.name();
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        LoadAuthentication loadAuthentication = reqUser.getLoadAuthentication();
        if (null == loadAuthentication) {
            context.setErrorCode(BusCode.A00062);
            return null;
        }
        ISecurityEasy4jUser userBy = loadAuthentication.getUserBy(reqUser);
        context.setDbUser(userBy);
        if (null == userBy) {
            context.setErrorCode(BusCode.A00037);
        }
        return userBy;
    }

    @Override
    public ISecurityEasy4jSession querySession(AuthenticationContext context) {
        ISecurityEasy4jUser dbUser = context.getDbUser();
        String username = dbUser.getUsername();
        if(StrUtil.isBlank(username)){
            context.setErrorCode(BusCode.A00063);
            return null;
        }
        SessionStrategy sessionStrategy = getSessionStrategy();
        ISecurityEasy4jSession session = sessionStrategy.getSessionByUserName(username);
        context.setDbSession(session);
        return session;
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
        LoadAuthentication loadAuthentication = reqUser.getLoadAuthentication();
        if (!loadAuthentication.verify(reqUser)) {
            String errorCode = reqUser.getErrorCode();
            if(StrUtil.isNotBlank(errorCode)){
                context.setErrorCode(errorCode);
            }else{
                context.setErrorCode(BusCode.A00033);
            }
        }
    }
}
