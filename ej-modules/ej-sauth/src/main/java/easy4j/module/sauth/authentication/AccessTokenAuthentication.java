package easy4j.module.sauth.authentication;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.AccessToken;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.RegexEscapeUtils;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.encryption.IPwdEncryptionService;
import easy4j.module.sauth.session.SessionStrategy;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * AccessToken访问的方式
 * 以口令来鉴权
 * 后台要配置token 对应的用户名，用户中文名  格式 AccessToken.username.usernameCn
 *
 * @author bokun.li
 * @date 2025-11-14
 */
public class AccessTokenAuthentication extends UserNamePasswordAuthentication {


    private static final Map<String, ISecurityEasy4jUser> USER_MAP = Maps.newConcurrentMap();

    @Override
    public String getName() {
        return AuthenticationType.AccessToken.name();
    }

    public ISecurityEasy4jUser getUserByAccessToken(String accessToken) {
        ISecurityEasy4jUser iSecurityEasy4jUser = USER_MAP.get(accessToken);
        if (null == iSecurityEasy4jUser) {
            synchronized (AccessTokenAuthentication.class) {
                if (USER_MAP.get(accessToken) == null) {
                    EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
                    List<AccessToken> simpleAuthAccessTokens = ejSysProperties.getSimpleAuthAccessTokens();
                    for (AccessToken simpleAuthAccessToken : simpleAuthAccessTokens) {
                        String token = simpleAuthAccessToken.getToken();
                        withToken(token);
                    }
                }
            }
        }
        return USER_MAP.get(accessToken);
    }

    private void withToken(String token) {
        if (StrUtil.startWith(token, "$")) {
            token = System.getenv(token.substring(1));
        }
        if (StrUtil.isBlank(token)) return;
        String[] split = token.split(RegexEscapeUtils.escapeRegex("."));
        String accessToken_ = ListTs.get(split, 0);
        String userName = ListTs.get(split, 1);
        userName = StrUtil.blankToDefault(userName, MD5.create().digestHex(accessToken_));
        String userNameCn = ListTs.get(split, 2);
        userNameCn = StrUtil.blankToDefault(userNameCn, "未知");
        SecurityUser securityUser = new SecurityUser();
        securityUser.setAccessToken(accessToken_);
        securityUser.setUsername(userName);
        securityUser.setUsernameCn(userNameCn);
        String salt = RandomUtil.randomString(4);
        securityUser.setPwdSalt(salt);
        securityUser.setCreateDate(new Date());
        securityUser.setAccountNonExpired(true);
        securityUser.setAccountNonLocked(true);
        securityUser.setCredentialsNonExpired(true);
        securityUser.setEnabled(true);
        securityUser.setExtMap(Maps.newHashMap());
        IPwdEncryptionService iPwdEncryptionService = SpringUtil.getBean(IPwdEncryptionService.class);
        String encrypt = iPwdEncryptionService.encrypt(accessToken_, securityUser);
        securityUser.setPassword(encrypt);
        USER_MAP.putIfAbsent(accessToken_, securityUser);
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        String accessToken = reqUser.getAccessToken();
        ISecurityEasy4jUser iSecurityEasy4jUser = getUserByAccessToken(accessToken);
        if (null == iSecurityEasy4jUser) {
            context.setErrorCode(BusCode.A00037);
            return null;
        }
        String username = iSecurityEasy4jUser.getUsername();
        if (StrUtil.isBlank(username)) {
            context.setErrorCode(BusCode.A00063);
            return null;
        }
        reqUser.setUsername(username);
        reqUser.setPassword(accessToken);
        context.setDbUser(iSecurityEasy4jUser);
        syncReqUser(context, iSecurityEasy4jUser);
        SessionStrategy sessionStrategy = getSessionStrategy();
        ISecurityEasy4jSession session = sessionStrategy.getSessionByUserName(username);
        context.setDbSession(session);

        return iSecurityEasy4jUser;
    }

    @Override
    public ISecurityEasy4jSession querySession(AuthenticationContext context) {
        return context.getDbSession();
    }
}
