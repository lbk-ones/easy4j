package easy4j.module.sauth.core;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import easy4j.module.sauth.authorization.AuthorizationStrategy;
import easy4j.module.sauth.domain.SecurityAuthority;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUserInfo;

import java.util.Set;

public abstract class StandardResolve {

    public abstract AuthorizationStrategy getAuthorizationStrategy();


    public SecurityUserInfo sessionToSecurityUserInfo(SecuritySession session) {
        String userName = session.getUserName();
        SecurityUserInfo securityUser = new SecurityUserInfo();
        AuthorizationStrategy authorizationStrategy = getAuthorizationStrategy();
        if (null != authorizationStrategy) {
            Set<SecurityAuthority> authorizationByUsername = authorizationStrategy.getAuthorizationByUsername(userName);
            securityUser.setAuthorities(CollUtil.isEmpty(authorizationByUsername) ? Sets.newHashSet() : authorizationByUsername);
        } else {
            securityUser.setAuthorities(Sets.newHashSet());
        }
        securityUser.setSkipAuthentication(false);
        securityUser.setPassword(null);
        securityUser.setNickName(null);
        securityUser.setShalt(null);
        securityUser.setCreateDate(null);
        securityUser.setUpdateDate(null);
        securityUser.setAccountNonExpired(true);
        securityUser.setAccountNonLocked(true);
        securityUser.setCredentialsNonExpired(true);
        securityUser.setEnabled(true);
        securityUser.setNickName(session.getNickName());

        securityUser.setUserId(session.getUserId());
        securityUser.setUsername(userName);
        securityUser.setUsernameCn(session.getUserNameCn());
        securityUser.setUsernameEn(session.getUserNameEn());
        // securityUser.setShaToken(session.getShaToken());
        return securityUser;
    }
}
