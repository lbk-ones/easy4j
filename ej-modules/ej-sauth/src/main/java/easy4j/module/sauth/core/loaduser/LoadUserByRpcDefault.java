package easy4j.module.sauth.core.loaduser;

import com.fasterxml.jackson.core.type.TypeReference;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import easy4j.module.sauth.config.Config;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.NacosInvokerApi;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityUser;
import jakarta.annotation.Resource;
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

    @Resource
    SecurityContext securityContext;

    String serverName;


    @Override
    public void afterPropertiesSet() throws Exception {

        // boolean property = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_SERVER, boolean.class);
        if (!isServer) {
            serverName = Config.AUTH_SERVER_NAME;
        }
    }


    @Override
    public ISecurityEasy4jUser loadUserByUserName(ISecurityEasy4jUser username) {
        ISecurityEasy4jUser user = securityContext.getUser(username.getUsername());
        if (user == null) {
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(serverName)
                    .path(LOAD_USER_BY_USER_NAME)
                    .body(username)
                    .isJson(true)
                    .build();
            String res = NacosInvokerApi.getEasy4jNacosInvokerApi().post(build);
            EasyResult<SecurityUser> object = JacksonUtil.toObject(res, new TypeReference<EasyResult<SecurityUser>>() {
            });
            CheckUtils.checkRpcRes(object);
            securityContext.setUser(username.getUsername(), object.getData());
        }
        return user;
    }
}
