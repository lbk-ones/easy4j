package easy4j.module.sauth.context;

import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUserInfo;

/**
 * 这一次请求的上下文
 */
public interface SecurityContext {

    SecuritySession getSession();

    SecurityUserInfo getSecurityUserInfo();

    void setSecurityUserInfo(SecurityUserInfo securityUserInfo);

}
