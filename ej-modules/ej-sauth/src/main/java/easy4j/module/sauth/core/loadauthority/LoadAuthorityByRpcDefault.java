package easy4j.module.sauth.core.loadauthority;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import easy4j.module.sauth.config.Config;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.NacosInvokerApi;
import easy4j.module.sauth.domain.SecurityAuthority;
import jakarta.annotation.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoadAuthorityByRpcDefault implements LoadAuthorityByRpc {

    public static final String LOAD_URL = "/sauth/loadSecurityAuthoritiesByUsername";

    @Resource
    SecurityContext securityContext;


    @Override
    public Set<SecurityAuthority> loadSecurityAuthoritiesByUsername(String userName) {
        Set<SecurityAuthority> authority = securityContext.getAuthority(userName);
        if (null == authority) {
            NacosInvokeDto build = NacosInvokeDto.builder()
                    .group(SysConstant.NACOS_AUTH_GROUP)
                    .serverName(Config.AUTH_SERVER_NAME)
                    .path(LOAD_URL + SP.SLASH + userName)
                    .build();
            String res = NacosInvokerApi.getEasy4jNacosInvokerApi().get(build);
            EasyResult<List<SecurityAuthority>> object = JacksonUtil.toObject(res, new TypeReference<EasyResult<List<SecurityAuthority>>>() {
            });
            CheckUtils.checkRpcRes(object);
            List<SecurityAuthority> data = object.getData();
            if(CollUtil.isNotEmpty(data)){
                HashSet<SecurityAuthority> securityAuthorities = new HashSet<>(data);
                securityContext.setAuthority(userName, securityAuthorities);
                return securityAuthorities;
            }
        }
        return new HashSet<>();
    }


}
