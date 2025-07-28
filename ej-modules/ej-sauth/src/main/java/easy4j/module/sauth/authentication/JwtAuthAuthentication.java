package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import easy4j.module.sauth.domain.SecuritySession;

import java.nio.charset.StandardCharsets;

/**
 * Jwt 认证方式 用于拦截器 和登录 两者并用
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public class JwtAuthAuthentication extends UserNamePasswordAuthentication {


    @Override
    public AuthenticationType getName() {
        return AuthenticationType.Jwt;
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        AuthenticationScopeType scope = reqUser.getScope();
        if (scope == AuthenticationScopeType.Authentication) {
            return super.queryUser(context);
        } else {
            String shaToken = reqUser.getShaToken();

            if (StrUtil.isBlank(shaToken)) {
                context.setErrorCode(BusCode.A00034);
                return null;
            }

            JWT jwt = JWT.of(shaToken);
            JWTPayload payload = jwt.getPayload();
            String userName = String.valueOf(payload.getClaim("un"));
            reqUser.setUsername(userName);
            reqUser.setUsernameCn(userName);
            ISecurityEasy4jUser byUserName = LoadUserApi.getByUserName(userName);
            context.setDbUser(byUserName);
            return byUserName;
        }
    }

    @Override
    public ISecurityEasy4jSession querySession(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        AuthenticationScopeType scope = reqUser.getScope();
        if (scope == AuthenticationScopeType.Authentication) {
            return super.querySession(context);
        } else {
            String username = reqUser.getUsername();
            if (StrUtil.isNotBlank(username)) {
                SecuritySession sessionByUserName = getSessionStrategy().getSessionByUserName(username);
                context.setDbSession(sessionByUserName);
                return sessionByUserName;
            }
        }
        return null;
    }

    @Override
    public void verifyPre(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        AuthenticationScopeType scope = reqUser.getScope();
        if (scope == AuthenticationScopeType.Authentication) {
            super.verifyPre(context);
        } else {
            // interceptor
            // check user exists
            ISecurityEasy4jUser dbUser = context.getDbUser();
            if (checkUserIsNotEnable(dbUser, context)) {
                return;
            }
            String shaToken = reqUser.getShaToken();
            JWT jwt = JWT.of(shaToken);
            String ejSysPropertyName = Easy4j.getEjSysPropertyName(EjSysProperties::getJwtSecret);
            String signatureSecret = Easy4j.getProperty(ejSysPropertyName);
            jwt.setKey(signatureSecret.getBytes(StandardCharsets.UTF_8));
            if (!jwt.verify()) {
                context.setErrorCode(BusCode.A00034);
                return;
            }
            // check session
            ISecurityEasy4jSession dbSession = context.getDbSession();
            checkSession(dbSession, context);
        }
    }

    @Override
    public void verify(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        AuthenticationScopeType scope = reqUser.getScope();
        if (scope == AuthenticationScopeType.Authentication) {
            super.verifyPre(context);
        }
    }

    @Override
    public OnlineUserInfo genOnlineUserInfo(AuthenticationContext context) {
        OnlineUserInfo onlineUserInfo = super.genOnlineUserInfo(context);
        ISecurityEasy4jUser user = onlineUserInfo.getUser();
        ISecurityEasy4jSession session = onlineUserInfo.getSession();
        String jwtToken = session.getJwtToken();
        // 返回jwtToken
        user.setShaToken(jwtToken);
        return onlineUserInfo;
    }
}
