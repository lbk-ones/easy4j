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
package easy4j.module.sauth.authentication;

import easy4j.module.sauth.authorization.SecurityAuthorization;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.EncryptionService;
import easy4j.module.sauth.domain.SecurityUserInfo;
import easy4j.module.sauth.session.SessionStrategy;

/**
 * DefaultSecurityAuthentication
 *
 * @author bokun.li
 * @date 2025-05
 */
public class DefaultSecurityAuthentication extends AbstractSecurityAuthentication {

    SecurityAuthorization authorizationStrategy;

    EncryptionService encryptionService;


    SessionStrategy sessionStrategy;

    SecurityContext securityContext;

    public DefaultSecurityAuthentication(SecurityAuthorization authorizationStrategy, EncryptionService encryptionService, SessionStrategy sessionStrategy, SecurityContext securityContext) {
        this.authorizationStrategy = authorizationStrategy;
        this.encryptionService = encryptionService;
        this.sessionStrategy = sessionStrategy;
        this.securityContext = securityContext;
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
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    @Override
    public EncryptionService getEncryptionService() {
        return encryptionService;
    }

    @Override
    public SecurityUserInfo getUserByUserName(String username) {
        return null;
    }
}
