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
package easy4j.module.sauth.context;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.Easy4jContextFactory;
import easy4j.infra.context.THConstant;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityAuthority;
import easy4j.module.sauth.domain.SecuritySession;

import java.util.Set;

/**
 * Easy4jSecurityContext
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Easy4jSecurityContext implements SecurityContext {

    @Override
    public ISecurityEasy4jSession getSession() {
        Easy4jContext context = Easy4jContextFactory.getContext();
        return (SecuritySession) context.getThreadHashValue(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY).orElse(null);
    }

    @Override
    public ISecurityEasy4jSession getSessionByToken(String byToken) {
        if (StrUtil.isBlank(byToken)) return null;
        Easy4jContext context = Easy4jContextFactory.getContext();
        return (SecuritySession) context.getThreadHashValue(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY + SP.DASH + byToken).orElse(null);
    }

    @Override
    public void setSession(ISecurityEasy4jSession securitySession) {
        if (null == securitySession) return;
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY, securitySession);
        String shaToken = securitySession.getShaToken();
        if (StrUtil.isNotBlank(shaToken)) {
            context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY + SP.DASH + shaToken, securitySession);
        }
    }

    @Override
    public void setSessionByToken(String byToken, ISecurityEasy4jSession securitySession) {
        if (StrUtil.isBlank(byToken) || null == securitySession) return;
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY + SP.DASH + byToken, securitySession);
    }

    @Override
    public void removeSession() {
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY, null);
    }

    @Override
    public void removeSessionByToken(String token) {
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY + SP.DASH + token, null);
    }

    @Override
    public void setUser(String userName, ISecurityEasy4jUser user) {
        if (StrUtil.isBlank(userName) || ObjectUtil.isEmpty(user)) return;
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_USERINFO_KEY + SP.DASH + userName, user);
    }

    @Override
    public ISecurityEasy4jUser getUser(String userName) {
        if (StrUtil.isBlank(userName)) return null;
        Easy4jContext context = Easy4jContextFactory.getContext();
        return (ISecurityEasy4jUser) context.getThreadHashValue(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_USERINFO_KEY + SP.DASH + userName).orElse(null);
    }

    @Override
    public void removeUser(String userName) {
        if (StrUtil.isBlank(userName)) return;
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_USERINFO_KEY + SP.DASH + userName, null);
    }

    @Override
    public void setAuthority(String userName, Set<SecurityAuthority> user) {
        if (StrUtil.isBlank(userName) || ObjectUtil.isEmpty(user)) return;
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_AUTHORITY_KEY + SP.DASH + userName, user);
    }

    @Override
    public Set<SecurityAuthority> getAuthority(String userName) {
        if (StrUtil.isBlank(userName)) return null;
        Easy4jContext context = Easy4jContextFactory.getContext();
        Object o = context.getThreadHashValue(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_AUTHORITY_KEY + SP.DASH + userName).orElse(null);
        if (o == null) return null;
        return Convert.toSet(SecurityAuthority.class, o);
    }

    @Override
    public void removeAuthority(String userName) {
        if (StrUtil.isBlank(userName)) return;
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(THConstant.EASY4J_SECURITY_CONTEXT_KEY, THConstant.EASY4J_SECURITY_CONTEXT_AUTHORITY_KEY + SP.DASH + userName, null);
    }
}
