package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SysConstant;
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.session.SessionStrategy;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Basic 认证方式 用于登录
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public class BasicAuthAuthentication extends AbstractAuthenticationCore {


    @Override
    public String getName() {
        return AuthenticationType.Basic.name();
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        HttpServletRequest servletRequest = getServletRequest();
        String basicToken = servletRequest.getHeader(SysConstant.AUTHORIZATION);
        if (basicToken == null || !basicToken.startsWith("Basic ")) {
            context.setErrorCode(BusCode.A00034);
            return null;
        }
        // extract username 、password and decode
        String base64Credentials = basicToken.substring("Basic ".length());
        String credentials = new String(
                Base64.getDecoder().decode(base64Credentials),
                StandardCharsets.UTF_8
        );
        // username:password
        final String[] values = credentials.split(":", 2);
        if (values.length != 2) {
            context.setErrorCode(BusCode.A00034);
            return null;
        }
        String username = values[0];
        reqUser.setUsername(username);
        String password = values[1];
        reqUser.setPassword(password);
        ISecurityEasy4jUser byUserName = LoadUserApi.getByUserName(username);
        syncReqUser(context, byUserName);
        context.setDbUser(byUserName);
        return byUserName;
    }

    @Override
    public ISecurityEasy4jSession querySession(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        String username = reqUser.getUsername();
        ISecurityEasy4jSession dbSession = context.getDbSession();
        if (null == dbSession) {
            SessionStrategy sessionStrategy = getSessionStrategy();
            SecuritySession session = sessionStrategy.getSessionByUserName(username);
            dbSession = session;
            context.setDbSession(session);
        }
        return dbSession;
    }

    @Override
    public void verifyPre(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        ISecurityEasy4jUser dbUser = context.getDbUser();
        // verify user enable
        if (checkUserIsNotEnable(dbUser, context)) {
            return;
        }
        // verify http method
        HttpServletRequest servletRequest = getServletRequest();
        String method = servletRequest.getMethod();
        if (!"post".equalsIgnoreCase(method)) {
            context.setErrorCode(BusCode.A00030);
            return;
        }
        String password = reqUser.getPassword();
        if (StrUtil.isBlank(password)) {
            context.setErrorCode(BusCode.A00032);
            return;
        }
        String password1 = dbUser.getPassword();
        if (StrUtil.isBlank(password1)) {
            context.setErrorCode(BusCode.A00032);
        }
    }

    @Override
    public void verify(AuthenticationContext context) {
        if (checkRepeatSession(context)) {
            return;
        }
        ISecurityEasy4jUser dbUser = context.getDbUser();
        ISecurityEasy4jUser reqUser = context.getReqUser();

        String encrypt = getPwdEncryptionService().encrypt(reqUser.getPassword(), dbUser);
        if (!StrUtil.equals(encrypt, dbUser.getPassword())) {
            context.setErrorCode(BusCode.A00033);
        }
    }
}
