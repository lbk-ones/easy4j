/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.sauth.core;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.domain.SecurityAuthority;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.domain.SecurityUserInfo;

import java.util.Set;

/**
 * StandardResolve
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class StandardResolve {

    public abstract SecurityAuthorization getAuthorizationStrategy();


    /**
     * 为了性能这里不查库 只单纯的转换
     *
     * @param session
     * @return
     */
    public SecurityUserInfo sessionToSecurityUserInfo(SecuritySession session) {
        String userName = session.getUserName();
        SecurityUserInfo securityUser = new SecurityUserInfo();
        SecurityAuthorization authorizationStrategy = getAuthorizationStrategy();
        if (null != authorizationStrategy) {
            Set<SecurityAuthority> authorizationByUsername = authorizationStrategy.getAuthorizationByUsername(userName);
            securityUser.setAuthorities(CollUtil.isEmpty(authorizationByUsername) ? Sets.newHashSet() : authorizationByUsername);
        } else {
            securityUser.setAuthorities(Sets.newHashSet());
        }
        securityUser.setSkipAuthentication(false);
        securityUser.setPassword(null);
        securityUser.setNickName(null);
        securityUser.setShalt(null);
        securityUser.setCreateDate(null);
        securityUser.setUpdateDate(null);
        securityUser.setAccountNonExpired(true);
        securityUser.setAccountNonLocked(true);
        securityUser.setCredentialsNonExpired(true);
        securityUser.setEnabled(true);
        securityUser.setNickName(session.getNickName());

        securityUser.setUserId(session.getUserId());
        securityUser.setUsername(userName);
        securityUser.setUsernameCn(session.getUserNameCn());
        securityUser.setUsernameEn(session.getUserNameEn());
        securityUser.setShaToken(session.getShaToken());
        securityUser.setJwtToken(session.getJwtToken());
        return securityUser;
    }
}
