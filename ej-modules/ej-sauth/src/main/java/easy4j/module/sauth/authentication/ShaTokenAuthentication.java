package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.session.SessionStrategy;

/**
 * sha-token认证方式 用于权限拦截
 * 这个 ShaToken其实就是给前端的那个Token 他可能是短的也可能是长的
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public class ShaTokenAuthentication extends AbstractAuthenticationCore {


    @Override
    public AuthenticationType getName() {
        return AuthenticationType.ShaToken;
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        String shaToken = reqUser.getShaToken();
        if (StrUtil.isBlank(shaToken)) {
            context.setErrorCode(BusCode.A00005);
            return null;
        }
        SessionStrategy sessionStrategy = getSessionStrategy();
        SecuritySession session = sessionStrategy.getSession(shaToken);
        context.setDbSession(session);
        if (null != session) {
            String userName = session.getUserName();
            ISecurityEasy4jUser byUserName = LoadUserApi.getByUserName(userName);
            context.setDbUser(byUserName);
            if (null == byUserName) {
                context.setErrorCode(BusCode.A00037);
            }
            return byUserName;
        } else {
            context.setErrorCode(BusCode.A00034);
        }
        return null;
    }

    @Override
    public ISecurityEasy4jSession querySession(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        String shaToken = reqUser.getShaToken();
        ISecurityEasy4jSession dbSession = context.getDbSession();
        if (null == dbSession) {
            SessionStrategy sessionStrategy = getSessionStrategy();
            SecuritySession session = sessionStrategy.getSession(shaToken);
            dbSession = session;
            context.setDbSession(session);
        }
        return dbSession;
    }

    @Override
    public void verifyPre(AuthenticationContext context) {
        ISecurityEasy4jUser dbUser = context.getDbUser();
        if (null == dbUser) {
            context.setErrorCode(BusCode.A00037);
            return;
        }
        ISecurityEasy4jSession dbSession = context.getDbSession();
        if (null == dbSession) {
            context.setErrorCode(BusCode.A00034);
        }

    }

    @Override
    public void verify(AuthenticationContext context) {
        ISecurityEasy4jUser dbUser = context.getDbUser();
        // verify user enable
        if (!checkUser(dbUser, context)) {
            return;
        }
        ISecurityEasy4jSession dbSession = context.getDbSession();
        checkSession(dbSession, context);
    }

    @Override
    public void refreshSession(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        String shaToken = reqUser.getShaToken();
        SessionStrategy sessionStrategy = getSessionStrategy();
        SecuritySession securitySession = sessionStrategy.refreshSession(shaToken, null, null);
        context.setDbSession(securitySession);
    }
}
