package easy4j.module.sauth.core.loaduser;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.sca.Easy4jNacosInvokerApi;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import easy4j.module.sauth.config.Config;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityUser;
import javax.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;

/**
 * 只从远程RPC服务中查询用户信息
 *
 * @author bokun.li
 * @date 2025-07-26
 */
public class LoadUserByRpcDefault implements LoadUserByRpc, InitializingBean {
    public static final String LOAD_USER_BY_USER_NAME = "/sauth/loadUserByUserName";
    public static final String LOAD_USER_BY_USER_ID = "/sauth/loadUserByUserId";


    @Override
    public boolean select() {
        boolean authEnable = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_ENABLE, boolean.class);
        // 只有客户端才能使用这个类查询
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_IS_SERVER, boolean.class);
        return authEnable && !isServer;
    }

    Easy4jNacosInvokerApi easy4jNacosInvokerApi;

    @Resource
    Easy4jContext easy4jContext;

    @Resource
    SecurityContext securityContext;

    String serverName;


    @Override
    public void afterPropertiesSet() throws Exception {
        easy4jNacosInvokerApi = easy4jContext.get(Easy4jNacosInvokerApi.class);

        // boolean property = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_SERVER, boolean.class);
        if (!isServer) {
            serverName = Config.AUTH_SERVER_NAME;
        }
    }


    @Override
    public ISecurityEasy4jUser loadUserByUserName(String username) {
        ISecurityEasy4jUser user = securityContext.getUser(username);
        if (user == null) {
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(LOAD_USER_BY_USER_NAME + SP.SLASH + username)
                    .build();
            EasyResult<Object> securitySessionEasyResult = easy4jNacosInvokerApi.get(build);
            CheckUtils.checkRpcRes(securitySessionEasyResult);
            user = CheckUtils.convertRpcRes(securitySessionEasyResult, SecurityUser.class);
            securityContext.setUser(username, user);
        }

        return user;
    }

    @Override
    public ISecurityEasy4jUser loadUserByUserId(long userId) {
        return null;
    }
}
