package easy4j.module.sauth.core;

import easy4j.module.base.context.Easy4jContext;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.BusCode;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;

/**
 * AbstractSecurityService
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class AbstractSecurityService extends StandardResolve implements SecurityService {

    public abstract SessionStrategy getSessionStrategy();

    public abstract SecurityContext getSecurityContext();


    @Override
    public SecurityUserInfo logoutByUserName(String userName) {
        SessionStrategy sessionStrategy = getSessionStrategy();
        SecuritySession sessionByUserName = sessionStrategy.getSessionByUserName(userName);
        if (sessionByUserName != null) {
            sessionStrategy.deleteSession(sessionByUserName.getShaToken());
            return sessionToSecurityUserInfo(sessionByUserName);
        } else {
            throw new EasyException(BusCode.A00037);
        }
    }

    @Override
    public SecurityUserInfo getOnlineUser() {
        SecurityContext securityContext = getSecurityContext();
        SecuritySession session = securityContext.getSession();
        if (session != null && session.isNotTampered() && session.isNotExpired()) {
            return sessionToSecurityUserInfo(session);
        }
        return null;
    }

    @Override
    public SecurityUserInfo getOnlineUser(String token) {
        SessionStrategy sessionStrategy = getSessionStrategy();
        SecuritySession session = sessionStrategy.getSession(token);
        if (session != null && session.isNotTampered() && session.isNotExpired()) {
            return sessionToSecurityUserInfo(session);
        }
        return null;
    }

    @Override
    public boolean isOnline(String token) {
        SessionStrategy sessionStrategy = getSessionStrategy();
        SecuritySession session = sessionStrategy.getSession(token);
        return session != null && session.isNotExpired();
    }
}