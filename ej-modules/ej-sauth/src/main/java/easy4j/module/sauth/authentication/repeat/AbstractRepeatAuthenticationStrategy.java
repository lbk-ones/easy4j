package easy4j.module.sauth.authentication.repeat;

import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.session.SessionStrategy;

public abstract class AbstractRepeatAuthenticationStrategy implements RepeatAuthenticationStrategy{


    private SessionStrategy sessionStrategy;


    protected SessionStrategy getSessionStrategy() {
        if(null==sessionStrategy){
            sessionStrategy = SpringUtil.getBean(SessionStrategy.class);
        }
        return sessionStrategy;
    }

    protected void saveSession(SecuritySession securitySession){
        getSessionStrategy().saveSession(securitySession);
    }


    protected void newSession(AuthenticationContext authenticationContext) {
        ISecurityEasy4jUser reqUser = authenticationContext.getReqUser();
        SecuritySession init = new SecuritySession().init(reqUser);
        saveSession(init);
        authenticationContext.setDbSession(init);
    }

}
