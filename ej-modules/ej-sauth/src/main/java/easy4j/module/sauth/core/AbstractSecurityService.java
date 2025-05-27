package easy4j.module.sauth.core;

import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;

public abstract class AbstractSecurityService implements SecurityService {

    public abstract SessionStrategy getSessionStrategy();

    public SecurityUserInfo sessionToSecurityUserInfo(SecuritySession session) {
        SecurityUserInfo securityUser = new SecurityUserInfo();
        securityUser.setUsername(session.getUserName());
        securityUser.setUsernameCn(session.getUserNameCn());
        securityUser.setUsernameEn(session.getUserNameEn());
        // securityUser.setShaToken(session.getShaToken());
        return securityUser;
    }

    @Override
    public SecurityUserInfo logoutByUserName(String userName) {

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
