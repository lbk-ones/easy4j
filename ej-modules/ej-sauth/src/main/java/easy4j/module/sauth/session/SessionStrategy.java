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

import easy4j.module.sauth.domain.SecuritySession;

import java.util.concurrent.TimeUnit;

/**
 * SessionStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface SessionStrategy {
    /**
     * 根据会话token获取会话信息
     *
     * @param token
     * @return
     */
    SecuritySession getSession(String token);

    /**
     * 根据用户名获取会话信息
     *
     * @param userName
     * @return
     */
    SecuritySession getSessionByUserName(String userName);

    /**
     * 保存会话信息
     *
     * @param securitySession
     * @return
     */
    SecuritySession saveSession(SecuritySession securitySession);


    /**
     * 删除会话信息
     *
     * @param token
     */
    void deleteSession(String token);

    /**
     * 刷新会话信息
     *
     * @param token
     * @param expireTime
     * @param timeUnit
     * @return
     */
    SecuritySession refreshSession(String token, Integer expireTime, TimeUnit timeUnit);


    void clearInValidSession();
}
