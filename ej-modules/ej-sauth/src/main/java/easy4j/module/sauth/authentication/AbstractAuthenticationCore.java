package easy4j.module.sauth.authentication;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.webmvc.CookieUtil;
import easy4j.infra.webmvc.WebContextUtil;
import easy4j.module.sauth.authentication.repeat.RepeatAuthentication;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.*;
import easy4j.module.sauth.encryption.IPwdEncryptionService;
import easy4j.module.sauth.filter.Easy4jSecurityFilterInterceptor;
import easy4j.module.sauth.session.SessionStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractAuthenticationCore implements AuthenticationCore {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SessionStrategy getSessionStrategy() {
        return SpringUtil.getBean(SessionStrategy.class);
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
        ISecurityEasy4jUser dbUser = context.getDbUser();
        if (dbUser != null) {
            Easy4jSecurityFilterInterceptor.bindUserCtx(dbUser);

            // 使用cookie
            if (context.getReqUser() != null) {
                ISecurityEasy4jUser reqUser = context.getReqUser();
                Boolean openApiAuthenticationIs = reqUser.getOpenApiAuthenticationIs();
                // 认证并且不是openApi的逻辑则写入Cookie
                if (reqUser.getScope() == AuthenticationScopeType.Authentication && !openApiAuthenticationIs) {
                    // 是否使用cookie
                    boolean useCookies = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_USE_COOKIE, boolean.class);
                    if (useCookies) {
                        // 是否使用httponly
                        boolean httpOnly = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_COOKIE_HTTPONLY, boolean.class, false);
                        // 是否使用secure模式
                        boolean secure = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_COOKIE_SECURE, boolean.class, false);
                        // domain
                        String domain = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_COOKIE_DOMAIN, String.class, null);
                        // 路径
                        String path = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_COOKIE_PATH, String.class, "/");
                        // 同源策略 默认Lax
                        String sameSite = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_COOKIE_SAME_SITE, String.class, "Lax");
                        // 过期时间
                        Integer expireTime = Easy4j.getProperty(SysConstant.EASY4J_AUTH_SESSION_EXPIRE_TIME, int.class);
                        ISecurityEasy4jSession dbSession = context.getDbSession();
                        String token = StrUtil.blankToDefault(dbSession.getShaToken(), dbUser.getShaToken());
                        if(StrUtil.isNotBlank(token)){
                            CookieUtil.setCookie(WebContextUtil.getResponse(), SysConstant.X_ACCESS_TOKEN, token, expireTime, path, domain, httpOnly, secure, sameSite);
                        }else{
                            logger.error("No token generation detected, skipping cookie writing");
                        }
                        // TODO refreshToken write
                    }
                }
            }
        }


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
        Set<SecurityAuthority> authorityList = onlineUserInfo.getAuthorityList();
        // user == dbUser
        ISecurityEasy4jUser user = onlineUserInfo.getUser();
        if (user != null) {
            if (CollUtil.isNotEmpty(authorityList)) {
                List<String> roleCodes = ListTs.mapDistinctToList(new ArrayList<>(authorityList), SecurityAuthority::getRoleCode);
                user.setRoleCodeList(roleCodes);
            }
            user.setShaToken(onlineUserInfo.getSession().getShaToken());
        }
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
        if (!accountNonExpired) {
            context.setErrorCode(BusCode.A00052);
            return true;
        }
        if (!credentialsNonExpired) {
            context.setErrorCode(BusCode.A00054);
            return true;
        }
        if (!enabled) {
            context.setErrorCode(BusCode.A00055);
            return true;
        }
        if (!accountNonLocked) {
            context.setErrorCode(BusCode.A00053);
            return true;
        }
        return false;
    }

    public boolean checkSessionIsValid(ISecurityEasy4jSession dbSession, AuthenticationContext context) {
        if (null == dbSession || !dbSession.isValid()) {
            context.setErrorCode(BusCode.A00035);
            return false;
        }
        return true;
    }


    public boolean checkRepeatSession(AuthenticationContext authenticationContext) {
        return !RepeatAuthentication.check(authenticationContext);
    }

    public void syncReqUser(AuthenticationContext context, ISecurityEasy4jUser dbUser) {
        if (null != dbUser) {
            ISecurityEasy4jUser reqUser = context.getReqUser();
            if (null != reqUser) {
                SecurityUser securityUser = new SecurityUser();
                // 再全拷贝一下 防止污染 dbUser 因为这个值很有可能是从外部传过来的
                BeanUtil.copyProperties(dbUser, securityUser);
                // 反向拷贝一下 以外部传进来的值为优先 没有的从查出来用户信息里面兼容
                BeanUtil.copyProperties(reqUser, securityUser, CopyOptions.create().ignoreNullValue());
                // 重新拷贝给reqUser
                BeanUtil.copyProperties(securityUser, reqUser);
            }
        }
    }
}
