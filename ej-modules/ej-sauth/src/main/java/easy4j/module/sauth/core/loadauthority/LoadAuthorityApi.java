package easy4j.module.sauth.core.loadauthority;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
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

    private static LoadAuthorityBy loadAuthorityBy;


    public static LoadAuthorityBy getLoadAuthorityBy() {
        try {
            if (loadAuthorityBy == null) {
                loadAuthorityBy = SpringUtil.getBean(LoadAuthorityBy.class);
            }
            return loadAuthorityBy;
        } catch (Exception ignored) {

        }
        return null;
    }

    public static Set<SecurityAuthority> getAuthorityList(String userName) {
        LoadAuthorityBy authorityBy = getLoadAuthorityBy();
        if (StrUtil.isNotBlank(userName) && null != authorityBy) {
            Set<SecurityAuthority> securityAuthorities = authorityBy.loadSecurityAuthoritiesByUsername(userName);
            if (CollUtil.isNotEmpty(securityAuthorities)) {
                return securityAuthorities;
            }
        }
        return new HashSet<>();
    }
}
