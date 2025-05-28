package easy4j.module.sauth.session;

import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.sauth.domain.SecuritySession;

import java.util.Objects;

public abstract class AbstractSessionStrategy implements SessionStrategy {

    @Override
    public SecuritySession refreshSession(String token) {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();

        SecuritySession securitySession1 = getSession(token);
        if (Objects.nonNull(securitySession1)) {
            deleteSession(token);
            int sessionExpireTimeSeconds = ejSysProperties.getSessionExpireTimeSeconds();
            securitySession1.setExpireTimeSeconds(sessionExpireTimeSeconds);
            saveSession(securitySession1);
        }
        return securitySession1;
    }

    @Override
    public void clearInValidSession() {

    }

    @Override
    public SecuritySession getSessionByUserName(String userName) {
        return null;
    }
}
