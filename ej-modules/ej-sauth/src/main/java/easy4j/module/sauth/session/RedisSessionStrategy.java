package easy4j.module.sauth.session;

import easy4j.module.sauth.domain.SecuritySession;

/**
 * RedisSessionStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
public class RedisSessionStrategy extends AbstractSessionStrategy {

    @Override
    public SecuritySession getSession(String token) {
        return null;
    }

    @Override
    public SecuritySession saveSession(SecuritySession securitySession) {
        return null;
    }

    @Override
    public void deleteSession(String token) {


    }


}