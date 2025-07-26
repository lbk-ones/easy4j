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
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.domain.*;

import java.util.Set;

/**
 * StandardResolve
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class StandardResolve {

    public abstract SecurityAuthorization getAuthorizationStrategy();

    public OnlineUserInfo sessionToSecurityUserInfo(SecuritySession session) {
        String userName = session.getUserName();
        ISecurityEasy4jUser byUserName = LoadUserApi.getByUserName(userName);
        byUserName.setShaToken(session.getShaToken());
        OnlineUserInfo onlineUserInfo = new OnlineUserInfo(session, byUserName);
        onlineUserInfo.handlerAuthorityList(userName);
        onlineUserInfo.handlerSession(userName);
        return onlineUserInfo;
    }
}
