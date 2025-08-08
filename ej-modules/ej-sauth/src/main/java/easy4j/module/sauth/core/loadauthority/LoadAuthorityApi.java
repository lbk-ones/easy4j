package easy4j.module.sauth.core.loadauthority;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.module.sauth.domain.SecurityAuthority;

import java.util.HashSet;
import java.util.Set;

/**
 * LoadAuthorityApi
 *
 * @author bokun.li
 * @date 2025-07-26
 */
public class LoadAuthorityApi {
    public static final String rpcBeanName = "loadAuthorityByRpc";

    private static LoadAuthorityByDb loadAuthorityByDb;


    private static final class LoadAuthorityByRpcHolder {
        static final LoadAuthorityByRpc loadAuthorityByRpc = SpringUtil.getBean(rpcBeanName);
    }

    public static LoadAuthorityByDb getLoadAuthorityByDb() {
        try {
            if (loadAuthorityByDb == null) {
                loadAuthorityByDb = SpringUtil.getBean(LoadAuthorityByDb.class);
            }
            return loadAuthorityByDb;
        } catch (Exception ignored) {

        }
        return null;
    }

    private static LoadAuthorityBy autoGetQuery(){
        boolean authEnable = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_ENABLE, boolean.class);
        if(!authEnable) return null;
        // only server query extra
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_IS_SERVER, boolean.class);
        if(isServer){
            return getLoadAuthorityByDb();
        }else{
            return LoadAuthorityByRpcHolder.loadAuthorityByRpc;
        }
    }

    // auto select query method
    public static Set<SecurityAuthority> getAuthorityList(String userName) {
        LoadAuthorityBy authorityBy = autoGetQuery();
        if (StrUtil.isNotBlank(userName) && null != authorityBy) {
            Set<SecurityAuthority> securityAuthorities = authorityBy.loadSecurityAuthoritiesByUsername(userName);
            if (CollUtil.isNotEmpty(securityAuthorities)) {
                return securityAuthorities;
            }
        }
        return new HashSet<>();
    }

    // directly use db query
    public static Set<SecurityAuthority> getAuthorityListByDb(String userName) {
        LoadAuthorityBy authorityBy = getLoadAuthorityByDb();
        if (StrUtil.isNotBlank(userName) && null != authorityBy) {
            Set<SecurityAuthority> securityAuthorities = authorityBy.loadSecurityAuthoritiesByUsername(userName);
            if (CollUtil.isNotEmpty(securityAuthorities)) {
                return securityAuthorities;
            }
        }
        return new HashSet<>();
    }
}
