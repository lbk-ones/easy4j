package easy4j.module.sauth.authentication;

import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;

/**
 * 用户名和密码的认证方式
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public class SimpleUserAuthentication extends UserNamePasswordAuthentication {


    @Override
    public String getName() {
        return AuthenticationType.Simple.name();
    }

    @Override
    public ISecurityEasy4jUser queryUser(AuthenticationContext context) {
        ISecurityEasy4jUser simpleUser = LoadUserApi.getSimpleUser();
        if (null == simpleUser) {
            context.setErrorCode(BusCode.A00037);
            return null;
        }
        context.setDbUser(simpleUser);
        syncReqUser(context,simpleUser);
        return simpleUser;
    }


}
