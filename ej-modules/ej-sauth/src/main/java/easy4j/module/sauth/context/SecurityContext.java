package easy4j.module.sauth.context;

import easy4j.module.sauth.domain.SecuritySession;

/**
 * 这一次请求的上下文
 */
public interface SecurityContext {

    SecuritySession getSession();

    void setSession(SecuritySession securitySession);

}
