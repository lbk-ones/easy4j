package easy4j.module.sauth.authentication.repeat;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;

public class RejectSessionStrategy extends AbstractRepeatAuthenticationStrategy{

    @Override
    public boolean checkRepeat(AuthenticationContext authenticationContext) {
        ISecurityEasy4jSession dbSession = authenticationContext.getDbSession();
        if(null!=dbSession){
            if(dbSession.isValid()){
                authenticationContext.setErrorCode(BusCode.A00044);
                return false;
            }else{
                // inValid delete session
                String shaToken = dbSession.getShaToken();
                if (StrUtil.isNotBlank(shaToken)) getSessionStrategy().deleteSession(shaToken);
                authenticationContext.setDbSession(null);
            }
        }
        return true;
    }
}
