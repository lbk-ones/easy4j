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
 * 如果是登录认证那么还是要传入用户名和密码相关
 * 如果是拦截器那么会从jwt里面解析出用户名
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public class JwtAuthAuthentication extends UserNamePasswordAuthentication {


    @Override
    public String getName() {
        return AuthenticationType.Jwt.name();
    }

    public String getJwtToken(AuthenticationContext context){
        ISecurityEasy4jUser reqUser = context.getReqUser();
        return reqUser.getShaToken();
    }

    @Override
    public ISecurityEasy4jSession querySession(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        AuthenticationScopeType scope = reqUser.getScope();
        if (scope == AuthenticationScopeType.Authentication) {
            return super.querySession(context);
        } else {
            // parse jwt token
            String jwtToken = getJwtToken(context);
            if (StrUtil.isBlank(jwtToken)) {
                context.setErrorCode(BusCode.A00034);
                return null;
            }
            JWT jwt = JWT.of(jwtToken);
            JWTPayload payload = jwt.getPayload();
            String userName = String.valueOf(payload.getClaim(JWTUtils.JWT_USER_NAME_KEY));
            String userNameCn = String.valueOf(payload.getClaim(JWTUtils.JWT_USER_NAME_CN_KEY));
            reqUser.setUsername(userName);
            reqUser.setUsernameCn(StrUtil.blankToDefault(userNameCn, userName));

            String username = reqUser.getUsername();
            if (StrUtil.isBlank(username)) {
                context.setErrorCode(BusCode.A00063);
                return null;
            }
            SecuritySession sessionByUserName = getSessionStrategy().getSessionByUserName(username);
            context.setDbSession(sessionByUserName);
            return sessionByUserName;
        }
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        AuthenticationScopeType scope = reqUser.getScope();
        if (scope == AuthenticationScopeType.Authentication) {
            return super.queryUser(context);
        } else {
            String username = reqUser.getUsername();
            ISecurityEasy4jUser byUserName = LoadUserApi.getByUserName(username);
            if (checkUserIsNotEnable(byUserName,context)) {
               return null;
            }
            syncReqUser(context, byUserName);
            context.setDbUser(byUserName);
            return byUserName;
        }
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
            String shaToken = getJwtToken(context);
            // verify
            if (!JWTUtils.verify(shaToken,null)) {
                context.setErrorCode(BusCode.A00034);
                return;
            }
            // check session
            ISecurityEasy4jSession dbSession = context.getDbSession();
            checkSessionIsValid(dbSession, context);
        }
    }

    @Override
    public void verify(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        AuthenticationScopeType scope = reqUser.getScope();
        if (scope == AuthenticationScopeType.Authentication) {
            super.verify(context);
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
