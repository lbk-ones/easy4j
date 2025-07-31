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
 * 用户名和密码的认证方式
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public class UserNamePasswordAuthentication extends AbstractAuthenticationCore {


    @Override
    public String getName() {
        return AuthenticationType.UserNamePassword.name();
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        String username = reqUser.getUsername();
        if (StrUtil.isBlank(username)) {
            context.setErrorCode(wrapException(BusCode.A00004, "username"));
            return null;
        }
        ISecurityEasy4jUser byUserName = LoadUserApi.getByUserName(username);
        context.setDbUser(byUserName);
        if (null == byUserName) {
            context.setErrorCode(BusCode.A00037);
        }
        return byUserName;
    }

    @Override
    public ISecurityEasy4jSession querySession(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        String username = reqUser.getUsername();
        SessionStrategy sessionStrategy = getSessionStrategy();
        ISecurityEasy4jSession session = sessionStrategy.getSessionByUserName(username);
        context.setDbSession(session);
        return session;
    }

    @Override
    public void verifyPre(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        // verify user enable
        if (checkUserIsNotEnable(context.getDbUser(), context)) {
            return;
        }
        verifyPre(reqUser);
        context.setErrorCode(reqUser.getErrorCode());
    }

    @Override
    public void verify(AuthenticationContext context) {
        String pwd = context.getReqUser().getPassword();
        ISecurityEasy4jUser dbUser = context.getDbUser();
        if (checkRepeatSession(context)) {
            return;
        }
        String encryptPwd = getPwdEncryptionService().encrypt(pwd, dbUser);
        if (!StrUtil.equals(encryptPwd, dbUser.getPassword())) {
            context.setErrorCode(BusCode.A00033);
        }


    }


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
            return;
        }
        String username = user.getUsername();
        String password = user.getPassword();
        if (StrUtil.isBlank(username)) {
            user.setErrorCode(BusCode.A00031);
            return;
        }
        if (StrUtil.isBlank(password)) {
            user.setErrorCode(BusCode.A00032);
        }
    }
}
