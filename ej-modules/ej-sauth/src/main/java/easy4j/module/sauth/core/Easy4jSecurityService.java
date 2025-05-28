package easy4j.module.sauth.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.plugin.i18n.I18nBean;
import easy4j.module.sauth.authentication.SecurityAuthentication;
import easy4j.module.sauth.authorization.AuthorizationStrategy;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;


public class Easy4jSecurityService extends AbstractSecurityService implements InitializingBean {


    SecurityAuthentication securityAuthentication;


    SessionStrategy sessionStrategy;


    AuthorizationStrategy authorizationStrategy;

    public Easy4jSecurityService(SecurityAuthentication securityAuthentication, SessionStrategy sessionStrategy, AuthorizationStrategy authorizationStrategy) {
        this.securityAuthentication = securityAuthentication;
        this.sessionStrategy = sessionStrategy;
        this.authorizationStrategy = authorizationStrategy;
    }

    private static DBAccess dbAccess;

    @Override
    public void afterPropertiesSet() throws Exception {

        dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class));
    }

    @Override
    public SecurityUserInfo getOnlineUser() {
        return null;
    }

    @Override
    public SessionStrategy getSessionStrategy() {
        return sessionStrategy;
    }

    @Override
    public AuthorizationStrategy getAuthorizationStrategy() {
        return authorizationStrategy;
    }

    @Override
    public SecurityUserInfo getOnlineUser(String token) {
        return null;
    }


    @Override
    public SecurityUserInfo login(SecurityUserInfo securityUser) {
        SecurityUserInfo securityUserInfo = securityAuthentication.verifyLoginAuthentication(securityUser);
        String errorCode = securityUserInfo.getErrorCode();
        if (StrUtil.isNotBlank(securityUserInfo.getErrorMsg()) || StrUtil.isNotBlank(errorCode)) {
            String message = securityUserInfo.getErrorMsg();
            if (StrUtil.isNotBlank(errorCode)) {
                message = I18nBean.getMessage(errorCode);
            }
            throw new EasyException(message);
        }
        if (!securityAuthentication.checkUser(securityUser)) {
            throw new EasyException("用户检查不通过");
        }
        SecuritySession init = new SecuritySession().init(securityUser);
        saveSession(init);
        securityUserInfo.setPassword(null);
        return securityUserInfo;
    }

    /**
     * 几种选择
     * 1、存入数据库
     * 2、存入redis
     * 3、存入内存
     *
     * @param init
     */
    private void saveSession(SecuritySession init) {

    }

    @Override
    public boolean isOnline(String token) {
        return false;
    }

    @Override
    public SecurityUserInfo logout() {
        return null;
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public String refreshToken(int expireTime, TimeUnit timeUnit) {
        return null;
    }
}
