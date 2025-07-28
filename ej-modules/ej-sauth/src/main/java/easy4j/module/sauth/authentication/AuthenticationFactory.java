package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ServiceLoaderUtils;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;

import java.util.List;
import java.util.Map;

/**
 * 认证器工厂
 *
 * @author bokun.li
 * @date 2025-07-27
 */
public class AuthenticationFactory {

    public static final String USERNAME_PASSWORD = "username";


    public static final Map<AuthenticationType, AuthenticationCore> authenticationMap = Maps.newHashMap();

    /**
     * 设置默认认证器，同时通过SPI加载默认扩展认证器
     */
    static {
        UserNamePasswordAuthentication userNamePasswordAuthentication = new UserNamePasswordAuthentication();
        ShaTokenAuthentication shaTokenAuthentication = new ShaTokenAuthentication();
        BasicAuthAuthentication basicAuthAuthentication = new BasicAuthAuthentication();
        SimpleUserAuthentication simpleUserAuthentication = new SimpleUserAuthentication();
        JwtAuthAuthentication jwtAuthAuthentication = new JwtAuthAuthentication();
        authenticationMap.put(userNamePasswordAuthentication.getName(), userNamePasswordAuthentication);
        authenticationMap.put(shaTokenAuthentication.getName(), shaTokenAuthentication);
        authenticationMap.put(basicAuthAuthentication.getName(), basicAuthAuthentication);
        authenticationMap.put(jwtAuthAuthentication.getName(), jwtAuthAuthentication);
        authenticationMap.put(simpleUserAuthentication.getName(), simpleUserAuthentication);
        List<AuthenticationCore> load = ServiceLoaderUtils.load(AuthenticationCore.class);
        for (AuthenticationCore authenticationCore : load) {
            AuthenticationType name = authenticationCore.getName();
            if (null != name) {
                authenticationMap.put(name, authenticationCore);
            }
        }
    }

    /**
     * 通过类型名获取认证器
     *
     * @param type
     * @return
     */
    public static AuthenticationCore get(AuthenticationType type) {
        AuthenticationCore authenticationCore = authenticationMap.get(type);
        if (authenticationCore == null) {
            throw EasyException.wrap(BusCode.A00047, type.name());
        }
        return authenticationCore;
    }


    /**
     * 获取上下文
     *
     * @param reqUser 传过来的用户信息
     * @return
     */
    public static AuthenticationContext ctx(ISecurityEasy4jUser reqUser) {
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setReqUser(reqUser);
        return authenticationContext;
    }


}
