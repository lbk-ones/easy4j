package easy4j.module.sauth.core;

import easy4j.module.sauth.domain.SecurityUserInfo;

/**
 * 加载用户信息
 *
 * @author bokun.li
 * @date 2025/6/17
 */
public interface LoadUserByUserName {

    SecurityUserInfo loadUserByUserName(String username);

}
