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


    private static final Map<String, AuthenticationCore> authenticationMap = Maps.newHashMap();

    /**
     * 设置默认认证器，同时通过SPI加载默认扩展认证器
     */
    static {
        UserNamePasswordAuthentication userNamePasswordAuthentication = new UserNamePasswordAuthentication();
        ShaTokenAuthentication shaTokenAuthentication = new ShaTokenAuthentication();
        BasicAuthAuthentication basicAuthAuthentication = new BasicAuthAuthentication();
        SimpleUserAuthentication simpleUserAuthentication = new SimpleUserAuthentication();
        JwtAuthAuthentication jwtAuthAuthentication = new JwtAuthAuthentication();
        OtherAuthentication otherAuthentication = new OtherAuthentication();
        BearerTokenAuthentication bearerTokenAuthentication = new BearerTokenAuthentication();
        BearerJwtTokenAuthAuthentication bearerJwtTokenAuthAuthentication = new BearerJwtTokenAuthAuthentication();
        AccessTokenAuthentication accessTokenAuthentication = new AccessTokenAuthentication();
        authenticationMap.put(userNamePasswordAuthentication.getName(), userNamePasswordAuthentication);
        authenticationMap.put(shaTokenAuthentication.getName(), shaTokenAuthentication);
        authenticationMap.put(basicAuthAuthentication.getName(), basicAuthAuthentication);
        authenticationMap.put(jwtAuthAuthentication.getName(), jwtAuthAuthentication);
        authenticationMap.put(simpleUserAuthentication.getName(), simpleUserAuthentication);
        authenticationMap.put(otherAuthentication.getName(), otherAuthentication);
        authenticationMap.put(bearerTokenAuthentication.getName(), bearerTokenAuthentication);
        authenticationMap.put(bearerJwtTokenAuthAuthentication.getName(), bearerJwtTokenAuthAuthentication);
        authenticationMap.put(accessTokenAuthentication.getName(), accessTokenAuthentication);
        List<AuthenticationCore> load = ServiceLoaderUtils.load(AuthenticationCore.class);
        for (AuthenticationCore authenticationCore : load) {
            String name = authenticationCore.getName();
            if (StrUtil.isNotBlank(name)) {
                authenticationMap.put(name, authenticationCore);
            }
        }
    }

    public static void register(String name,AuthenticationCore core){
        authenticationMap.putIfAbsent(name,core);
    }

    /**
     * 通过类型名获取认证器
     *
     * @param type
     * @return
     */
    public static AuthenticationCore get(String type) {
        AuthenticationCore authenticationCore = authenticationMap.get(type);
        if (authenticationCore == null) {
            throw EasyException.wrap(BusCode.A00047, type);
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
