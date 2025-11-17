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

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.authentication.AuthenticationContext;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import easy4j.module.sauth.domain.SecuritySession;
import easy4j.module.sauth.session.SessionStrategy;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AbstractSecurityService
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class AbstractSecurityService extends StandardResolve implements SecurityService {

    public abstract SessionStrategy getSessionStrategy();

    public abstract SecurityContext getSecurityContext();


    @Override
    public OnlineUserInfo logoutByUserName(String userName) {
        SessionStrategy sessionStrategy = getSessionStrategy();
        SecuritySession sessionByUserName = sessionStrategy.getSessionByUserName(userName);
        if (sessionByUserName != null) {
            sessionStrategy.deleteSession(sessionByUserName.getShaToken());
            getSecurityContext().removeSession();
            return sessionToSecurityUserInfo(sessionByUserName);
        } else {
            throw new EasyException(BusCode.A00037);
        }
    }

    @Override
    public OnlineUserInfo getOnlineUser() {
        SecurityContext securityContext = getSecurityContext();
        ISecurityEasy4jSession session = securityContext.getSession();
        if (session != null && session.isValid()) {
            return sessionToSecurityUserInfo(session);
        }
        return null;
    }

    @Override
    public OnlineUserInfo getOnlineUser(String token) {
        SessionStrategy sessionStrategy = getSessionStrategy();
        SecuritySession session = sessionStrategy.getSession(token);
        if (session != null && session.isValid()) {
            return sessionToSecurityUserInfo(session);
        }
        return null;
    }

    @Override
    public boolean isOnline(String token) {
        SessionStrategy sessionStrategy = getSessionStrategy();
        SecuritySession session = sessionStrategy.getSession(token);
        return session != null && session.isValid();
    }

    // 默认实现 返回null
    @Override
    public OnlineUserInfo authentication(ISecurityEasy4jUser securityUser, Consumer<AuthenticationContext> loginAware) {
        // by sub class impl
        return null;
    }

    @Override
    public OnlineUserInfo logout() {
        OnlineUserInfo onlineUser = getOnlineUser();
        if (onlineUser != null) {
            String shaToken = onlineUser.getSession().getShaToken();
            SessionStrategy sessionStrategy = getSessionStrategy();
            if (Objects.nonNull(sessionStrategy) && StrUtil.isNotBlank(shaToken)) {
                sessionStrategy.deleteSession(shaToken);
                getSecurityContext().removeSession();
            }
        }
        return onlineUser;
    }

    @Override
    public String getToken() {
        OnlineUserInfo onlineUser = getOnlineUser();
        if (null != onlineUser) {
            ISecurityEasy4jSession session = onlineUser.getSession();
            if (null != session) {
                return session.getShaToken();
            } else {
                ISecurityEasy4jUser user = onlineUser.getUser();
                if (null != user) {
                    return user.getShaToken();
                }
            }
        }
        return null;
    }

    @Override
    public String refreshToken(int expireTime, TimeUnit timeUnit) {
        OnlineUserInfo onlineUser = getOnlineUser();
        if (null != onlineUser) {
            String shaToken = onlineUser.getSession().getShaToken();
            SessionStrategy sessionStrategy = getSessionStrategy();
            sessionStrategy.refreshSession(shaToken, expireTime, timeUnit);
        }
        return null;
    }
}
