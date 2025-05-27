package easy4j.module.sauth.session;

import easy4j.module.sauth.domain.SecuritySession;

public class DbSessionStrategy extends AbstractSessionStrategy {


    @Override
    public SecuritySession getSession(String token) {
        return null;
    }

    @Override
    public SecuritySession saveSession(SecuritySession securitySession) {
        return null;
    }

    @Override
    public SecuritySession deleteSession(String token) {
        return null;
    }
}
