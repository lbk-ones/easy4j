package easy4j.module.sauth.authentication.repeat;

import cn.hutool.core.util.StrUtil;
import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecuritySession;
/**
 * 这个弊端很大 容易混乱，造成会话的误杀
 *
 * @author bokun.li
 * @date 2025/7/31
 */
public class NewSessionStrategy extends AbstractRepeatAuthenticationStrategy{
    @Override
    public boolean checkRepeat(AuthenticationContext authenticationContext) {
        ISecurityEasy4jSession dbSession = authenticationContext.getDbSession();
        if(null==dbSession){
            return true;
        }

        if (!dbSession.isValid()) {
            String shaToken = dbSession.getShaToken();
            if(StrUtil.isNotBlank(shaToken)) getSessionStrategy().deleteSession(shaToken);
        }

        // 重新写入会话
        newSession(authenticationContext);

        return true;
    }


}
