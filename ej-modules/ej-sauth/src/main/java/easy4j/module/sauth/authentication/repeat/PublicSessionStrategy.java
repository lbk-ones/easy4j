package easy4j.module.sauth.authentication.repeat;

import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;

/**
 * 会话共存，每次登录都拿取同一个会话，同时刷新会话，如果上一个会话失效那么生成新会话
 *
 * @author bokun.li
 * @date 2025/7/31
 */
public class PublicSessionStrategy extends AbstractRepeatAuthenticationStrategy{

    @Override
    public boolean checkRepeat(AuthenticationContext authenticationContext) {
        ISecurityEasy4jSession dbSession = authenticationContext.getDbSession();
        if(null!=dbSession){
            String shaToken = dbSession.getShaToken();
            if(dbSession.isValid()){
                getSessionStrategy().refreshSession(shaToken,null,null);
            }else{
                getSessionStrategy().deleteSession(shaToken);
                newSession(authenticationContext);
            }
        }
        return true;
    }

}
