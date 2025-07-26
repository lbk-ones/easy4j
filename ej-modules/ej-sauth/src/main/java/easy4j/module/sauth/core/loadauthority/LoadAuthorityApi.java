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


    public LoadAuthorityBy getLoadAuthorityBy() {
        if (loadAuthorityBy == null) {
            loadAuthorityBy = SpringUtil.getBean(LoadAuthorityBy.class);
        }
        return loadAuthorityBy;
    }

    public static Set<SecurityAuthority> getAuthorityList(String userName) {
        if (loadAuthorityBy != null && StrUtil.isNotBlank(userName)) {
            Set<SecurityAuthority> securityAuthorities = loadAuthorityBy.loadSecurityAuthoritiesByUsername(userName);
            if (CollUtil.isNotEmpty(securityAuthorities)) {
                return securityAuthorities;
            }
        }

        return new HashSet<>();
    }
}
