package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.utils.BusCode;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.EncryptionService;
import easy4j.module.sauth.core.StandardResolve;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractSecurityAuthentication extends StandardResolve implements SecurityAuthentication {

    public abstract EncryptionService getEncryptionService();

    public abstract SessionStrategy getSessionStrategy();


    public abstract SecurityContext getSecurityContext();

    public HttpServletRequest getServletRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        assert sra != null;
        return (HttpServletRequest) sra.getRequest();
    }

    // 校验密码是否准确
    // 动态查询患者信息
    // 动态选择加密方式
    @Override
    public SecurityUserInfo verifyLoginAuthentication(SecurityUserInfo user) {
        if (null == user) {
            SecurityUserInfo securityUserInfo = new SecurityUserInfo();
            securityUserInfo.setErrorCode(BusCode.A00004 + ",user");
            return securityUserInfo;
        }
        HttpServletRequest servletRequest = getServletRequest();
        String method = servletRequest.getMethod();
        if ("POST".equalsIgnoreCase(method)) {
            user.setErrorCode(BusCode.A00030);
            return user;
        }
        String username = user.getUsername();
        String password = user.getPassword();
        if (StrUtil.isBlank(username)) {
            user.setErrorCode(BusCode.A00031);
            return user;
        }
        boolean isSkip = user.isSkipPassword();
        if (StrUtil.isBlank(password) && !isSkip) {
            user.setErrorCode(BusCode.A00032);
            return user;
        }
        SecurityUserInfo userByUserName = getUserByUserName(username);
        if (userByUserName == null) {
            return user;
        }
        // 跳过密码直接认证成功
        if (!isSkip) {
            String encryptPwd = getEncryptionService().encrypt(password, user);
            if (StrUtil.equals(encryptPwd, userByUserName.getPassword())) {
                return userByUserName;
            } else {
                user.setErrorCode(BusCode.A00033);
            }
        }

        return user;

    }


    /**
     * 默认检查通过 如果想更改可以覆盖
     *
     * @param user
     * @return
     * @throws EasyException
     */
    @Override
    public boolean checkUser(SecurityUserInfo user) throws EasyException {
        return true;
    }


    /**
     * 根据token重新鉴权
     *
     * @param token
     * @return
     * @throws EasyException
     */
    @Override
    public SecurityUserInfo tokenAuthentication(String token) throws EasyException {
        SecurityUserInfo securityUserInfo = new SecurityUserInfo();
        SessionStrategy securitySession = getSessionStrategy();
        SecuritySession session = securitySession.getSession(token);
        if (session == null) {
            securityUserInfo.setErrorCode(BusCode.A00034);
            return securityUserInfo;
        }
        if (session.isNotExpired()) {
            securityUserInfo.setErrorCode(BusCode.A00035);
            return securityUserInfo;
        }
        securityUserInfo = sessionToSecurityUserInfo(session);
        SecurityUserInfo userByUserName = getUserByUserName(session.getUserName());
        if (getAuthorizationStrategy().checkByUserInfo(userByUserName)) {
            // refresh session
            session = securitySession.refreshSession(token);
            getSecurityContext().setSession(session);
        }
        return securityUserInfo;
    }
}
