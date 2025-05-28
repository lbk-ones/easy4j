package easy4j.module.sauth.authentication;

import easy4j.module.sauth.authorization.AuthorizationStrategy;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.EncryptionService;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;

public class DefaultSecurityAuthentication extends AbstractSecurityAuthentication {

    AuthorizationStrategy authorizationStrategy;

    EncryptionService encryptionService;


    SessionStrategy sessionStrategy;

    SecurityContext securityContext;

    public DefaultSecurityAuthentication(AuthorizationStrategy authorizationStrategy, EncryptionService encryptionService, SessionStrategy sessionStrategy, SecurityContext securityContext) {
        this.authorizationStrategy = authorizationStrategy;
        this.encryptionService = encryptionService;
        this.sessionStrategy = sessionStrategy;
        this.securityContext = securityContext;
    }

    @Override
    public AuthorizationStrategy getAuthorizationStrategy() {
        return authorizationStrategy;
    }

    @Override
    public SessionStrategy getSessionStrategy() {
        return sessionStrategy;
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    @Override
    public EncryptionService getEncryptionService() {
        return encryptionService;
    }

    @Override
    public SecurityUserInfo getUserByUserName(String username) {
        return null;
    }
}
