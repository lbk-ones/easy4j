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

import easy4j.module.base.context.Easy4jContext;
import easy4j.module.base.context.Easy4jContextFactory;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.sauth.domain.SecuritySession;

/**
 * Easy4jSecurityContext
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Easy4jSecurityContext implements SecurityContext {

    @Override
    public SecuritySession getSession() {
        Easy4jContext context = Easy4jContextFactory.getContext();
        return (SecuritySession) context.getThreadHashValue(SysConstant.EASY4J_SECURITY_CONTEXT_KEY, SysConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY).orElse(null);
    }

    @Override
    public void setSession(SecuritySession securitySession) {
        Easy4jContext context = Easy4jContextFactory.getContext();
        context.registerThreadHash(SysConstant.EASY4J_SECURITY_CONTEXT_KEY, SysConstant.EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY, securitySession);
    }
}
