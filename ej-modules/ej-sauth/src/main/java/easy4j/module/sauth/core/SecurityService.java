package easy4j.module.sauth.core;

import easy4j.module.sauth.domain.SecurityUserInfo;

import java.util.concurrent.TimeUnit;

/**
 * 核心类
 * 权限接口
 */
public interface SecurityService {

    SecurityUserInfo getOnlineUser();

    SecurityUserInfo getOnlineUser(String token);

    boolean isOnline(String token);

    /**
     * 登录
     */
    SecurityUserInfo login(SecurityUserInfo securityUser);

    SecurityUserInfo logout();

    SecurityUserInfo logoutByUserName(String userName);

    String getToken();

    String refreshToken(int expireTime, TimeUnit timeUnit);

}
