package easy4j.module.sauth.core.loaduser;

import easy4j.module.sauth.domain.ISecurityEasy4jUser;

/**
 * 加载用户信息
 *
 * @author bokun.li
 * @date 2025/6/17
 */
public interface LoadUserBy {

    boolean select();


    ISecurityEasy4jUser loadUserByUserName(String username);

    ISecurityEasy4jUser loadUserByUserId(long userId);
}
