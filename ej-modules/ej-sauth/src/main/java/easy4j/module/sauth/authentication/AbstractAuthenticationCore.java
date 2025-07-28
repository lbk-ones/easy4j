package easy4j.module.sauth.authentication;

import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import easy4j.module.sauth.encryption.IPwdEncryptionService;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractAuthenticationCore implements AuthenticationCore {

    public SessionStrategy getSessionStrategy() {
        return SpringUtil.getBean(SessionStrategy.class);
    }


    public HttpServletRequest getServletRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        assert sra != null;
        return (HttpServletRequest) sra.getRequest();
    }

    public SecurityContext getSecurityContext() {
        return SpringUtil.getBean(SecurityContext.class);
    }

    protected void bindCtx(ISecurityEasy4jSession init) {
        SecurityContext securityContext1 = getSecurityContext();
        securityContext1.setSession(init);
    }

    protected IPwdEncryptionService getPwdEncryptionService() {
        return SpringUtil.getBean(IPwdEncryptionService.class);
    }

    @Override
    public boolean checkUser(AuthenticationContext context) {
        return true;
    }

    @Override
    public void bindSessionToCtx(AuthenticationContext context) {
        bindCtx(context.getDbSession());
    }

    @Override
    public void refreshSession(AuthenticationContext context) {
        // not do something
    }


    @Override
    public OnlineUserInfo genOnlineUserInfo(AuthenticationContext context) {
        ISecurityEasy4jSession dbSession = context.getDbSession();
        ISecurityEasy4jUser dbUser = context.getDbUser();
        OnlineUserInfo onlineUserInfo = new OnlineUserInfo(dbSession, dbUser);
        onlineUserInfo.handlerAuthorityList(dbUser.getUsername());
        onlineUserInfo.handlerSession(dbUser.getUsername());
        onlineUserInfo.handlerUserInfo(dbUser.getUsername());
        ISecurityEasy4jUser user = onlineUserInfo.getUser();
        user.setShaToken(onlineUserInfo.getSession().getShaToken());
        context.setOnlineUserInfo(onlineUserInfo);
        return onlineUserInfo;
    }


    String wrapException(String code, String... args) {
        EasyException wrap = EasyException.wrap(code, args);
        return wrap.getMessage();
    }

    public boolean checkUserIsNotEnable(ISecurityEasy4jUser dbUser, AuthenticationContext context) {
        if (null == dbUser) {
            context.setErrorCode(BusCode.A00037);
            return true;
        }
        boolean accountNonExpired = dbUser.isAccountNonExpired();
        boolean accountNonLocked = dbUser.isAccountNonLocked();
        boolean credentialsNonExpired = dbUser.isCredentialsNonExpired();
        boolean enabled = dbUser.isEnabled();
        if(!accountNonExpired){
            context.setErrorCode(BusCode.A00052);
            return true;
        }
        if(!credentialsNonExpired){
            context.setErrorCode(BusCode.A00054);
            return true;
        }
        if(!enabled){
            context.setErrorCode(BusCode.A00055);
            return true;
        }
        if(!accountNonLocked){
            context.setErrorCode(BusCode.A00053);
            return true;
        }
        return false;
    }

    public boolean checkSession(ISecurityEasy4jSession dbSession, AuthenticationContext context) {
        if (null == dbSession || !dbSession.isValid()) {
            context.setErrorCode(BusCode.A00035);
            return false;
        }
        return true;
    }
}
