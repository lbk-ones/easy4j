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
package easy4j.module.security.core;

import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.AbstractSecurityService;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * SpringSecurityService
 *
 * @author bokun.li
 * @date 2025-05
 */
@Service
public class SpringSecurityService extends AbstractSecurityService {

    AuthenticationManager authenticationManager;

    SessionStrategy sessionStrategy;


    SecurityAuthorization authorizationStrategy;
    SecurityContext securityContext;

    @Autowired
    public SpringSecurityService(
            AuthenticationManager authenticationManager,
            SessionStrategy sessionStrategy,
            SecurityAuthorization authorizationStrategy,
            SecurityContext securityContext
    ) {
        this.authenticationManager = authenticationManager;
        this.sessionStrategy = sessionStrategy;
        this.authorizationStrategy = authorizationStrategy;
        this.securityContext = securityContext;
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    @Override
    public SecurityAuthorization getAuthorizationStrategy() {
        return authorizationStrategy;
    }

    @Override
    public SessionStrategy getSessionStrategy() {
        return sessionStrategy;
    }

    @Override
    public SecurityUserInfo getOnlineUser() {
        return null;
    }

    @Override
    public SecurityUserInfo getOnlineUser(String token) {
        return null;
    }

    @Override
    public boolean isOnline(String token) {
        return false;
    }

    @Override
    public SecurityUserInfo logout() {
        return null;
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public String refreshToken(int expireTime, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public SecurityUserInfo login(SecurityUserInfo securityUser) {
        String username = securityUser.getUsername();
        String password = securityUser.getPassword();
        // 1. 创建认证请求
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
//
//        // 2. 认证成功，设置SecurityContext
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        // 3. 生成JWT令牌
//        String jwt = jwtUtils.generateJwtToken(authentication);
//
//        // 4. 获取用户信息
//        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//        List<String> roles = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
        return null;
    }
}
