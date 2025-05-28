package easy4j.module.sauth.context;

import easy4j.module.base.context.Easy4jContext;
import easy4j.module.base.context.Easy4jContextFactory;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.sauth.domain.SecuritySession;

public class Easy4jSecurityContext implements SecurityContext {

    @Override
    public SecuritySession getSession() {
        Easy4jContext context = Easy4jContextFactory.getContext();
        return (SecuritySession) context.getThreadHashValue(SysConstant.EASY4J_SECURITY_CONTEXT_KEY, SysConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY).orElse(null);
    }

    @Override
    public void setSession(SecuritySession securitySession) {
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(SysConstant.EASY4J_SECURITY_CONTEXT_KEY, SysConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY, securitySession);
    }
}
