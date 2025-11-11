package easy4j.module.sauth.core.loadauthority;

import cn.hutool.core.util.ObjectUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.sca.Easy4jNacosInvokerApi;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import easy4j.module.sauth.config.Config;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.SecurityAuthority;
import org.springframework.beans.factory.InitializingBean;

import jakarta.annotation.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoadAuthorityByRpcDefault implements LoadAuthorityByRpc, InitializingBean {

    public static final String LOAD_URL = "/sauth/loadSecurityAuthoritiesByUsername";

    Easy4jNacosInvokerApi easy4jNacosInvokerApi;

    @Resource
    Easy4jContext easy4jContext;

    @Resource
    SecurityContext securityContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        easy4jNacosInvokerApi = easy4jContext.get(Easy4jNacosInvokerApi.class);
    }

    @Override
    public Set<SecurityAuthority> loadSecurityAuthoritiesByUsername(String userName) {
        Set<SecurityAuthority> authority = securityContext.getAuthority(userName);
        if (null == authority) {
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(Config.AUTH_SERVER_NAME)
                    .path(LOAD_URL + SP.SLASH + userName)
                    .build();
            EasyResult<Object> securitySessionEasyResult = easy4jNacosInvokerApi.get(build);
            CheckUtils.checkRpcRes(securitySessionEasyResult);
            Object data = securitySessionEasyResult.getData();
            if (ObjectUtil.isNotEmpty(data)) {
                List<SecurityAuthority> list = JacksonUtil.toList(JacksonUtil.toJson(data), SecurityAuthority.class);
                HashSet<SecurityAuthority> securityAuthorities = new HashSet<>(list);
                securityContext.setAuthority(userName, securityAuthorities);
                return securityAuthorities;
            }
        }
        return new HashSet<>();
    }

}
