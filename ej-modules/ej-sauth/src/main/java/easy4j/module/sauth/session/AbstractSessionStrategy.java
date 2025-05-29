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
package easy4j.module.sauth.session;

import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.sauth.domain.SecuritySession;

import java.util.Objects;

/**
 * AbstractSessionStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class AbstractSessionStrategy implements SessionStrategy {

    @Override
    public SecuritySession refreshSession(String token) {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();

        SecuritySession securitySession1 = getSession(token);
        if (Objects.nonNull(securitySession1)) {
            deleteSession(token);
            int sessionExpireTimeSeconds = ejSysProperties.getSessionExpireTimeSeconds();
            securitySession1.setExpireTimeSeconds(sessionExpireTimeSeconds);
            saveSession(securitySession1);
        }
        return securitySession1;
    }

    @Override
    public void clearInValidSession() {

    }

    @Override
    public SecuritySession getSessionByUserName(String userName) {
        return null;
    }
}
