package easy4j.module.sauth.authentication.repeat;

import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;

/**
 * 把已存在的会话踢下线，线上环境一般是这个
 *
 * @author bokun.li
 * @date 2025/7/31
 */
public class KickSessionStrategy extends AbstractRepeatAuthenticationStrategy{

    @Override
    public boolean checkRepeat(AuthenticationContext authenticationContext) {
        ISecurityEasy4jSession dbSession = authenticationContext.getDbSession();
        if(null!=dbSession){
            getSessionStrategy().deleteSession(dbSession.getShaToken());

            // 重新写入会话
            newSession(authenticationContext);
        }
        return true;
    }

}
