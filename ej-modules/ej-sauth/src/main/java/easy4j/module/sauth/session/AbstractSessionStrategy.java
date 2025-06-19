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

import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.module.sauth.domain.SecuritySession;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * AbstractSessionStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class AbstractSessionStrategy implements SessionStrategy {

    public List<SecuritySession> cacheSessionList = ListTs.newCopyOnWriteArrayList();


    @Override
    public SecuritySession refreshSession(String token, Integer expireTime, TimeUnit timeUnit) {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();

        SecuritySession securitySession1 = getSession(token);
        if (Objects.nonNull(securitySession1)) {
            long expireTimeSeconds = securitySession1.getExpireTimeSeconds();
            int sessionRefreshTimeRemaining = ejSysProperties.getSessionRefreshTimeRemaining();
            // 还剩10分钟的时候刷新会话
            if (expireTimeSeconds > 0 && (new Date().getTime() + sessionRefreshTimeRemaining * 1000L) >= expireTimeSeconds) {
                deleteSession(token);
                long sessionExpireTimeSeconds = new Date().getTime() + (ejSysProperties.getSessionExpireTimeSeconds() * 1000L);
                if (null != expireTime && timeUnit != null) {
                    long millis = new Date().getTime() + timeUnit.toMillis(expireTime);
                    securitySession1.setExpireTimeSeconds(millis);
                } else {
                    securitySession1.setExpireTimeSeconds(sessionExpireTimeSeconds);
                }
                saveSession(securitySession1);
            }
        }
        return securitySession1;
    }

    @Override
    public void clearInValidSession() {
        for (int i = cacheSessionList.size() - 1; i > 0; i--) {
            SecuritySession securitySession = cacheSessionList.get(i);
            if (securitySession == null) {
                cacheSessionList.remove(i);
                continue;
            }
            if (!securitySession.isValid()) {
                deleteSession(securitySession.getShaToken());
                cacheSessionList.remove(i);
            }
        }
    }

    @Override
    public SecuritySession getSessionByUserName(String userName) {
        return null;
    }

    @Override
    public SecuritySession saveSession(SecuritySession securitySession) {
        if (null != securitySession) {
            cacheSessionList.add(securitySession);
        }
        return null;
    }
}
