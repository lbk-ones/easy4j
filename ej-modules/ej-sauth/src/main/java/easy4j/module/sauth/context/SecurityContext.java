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

import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityAuthority;

import java.util.Set;

/**
 * 这一次请求的上下文
 */
public interface SecurityContext {

    ISecurityEasy4jSession getSession();

    void setSession(ISecurityEasy4jSession securitySession);

    ISecurityEasy4jSession getSessionByToken(String byToken);

    void setSessionByToken(String byToken, ISecurityEasy4jSession securitySession);

    void setUser(String userName, ISecurityEasy4jUser user);

    ISecurityEasy4jUser getUser(String userName);

    void removeUser(String userName);

    void removeSession();

    void removeSessionByToken(String token);


    void setAuthority(String userName, Set<SecurityAuthority> user);

    Set<SecurityAuthority> getAuthority(String userName);

    void removeAuthority(String userName);

}
