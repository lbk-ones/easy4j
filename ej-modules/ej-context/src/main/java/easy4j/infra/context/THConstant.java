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
package easy4j.infra.context;

import jodd.util.StringPool;

import static easy4j.infra.common.utils.SysConstant.PARAM_PREFIX;

/**
 * THConstant
 *
 * @author bokun.li
 * @date 2025-08-09
 */
public class THConstant {


    public static final String EASY4J_RPC_NO_LOGIN = "X-Rpc-No-Login";
    public static final String EASY4J_IS_NO_LOGIN = "X-Rpc-Is-No-Login";

    public static final String EASY4J_RPC_TRACE = PARAM_PREFIX + StringPool.DOT + "rpc-trace-id";


    public static final String EASY4J_SECURITY_CONTEXT_KEY = PARAM_PREFIX + StringPool.DOT + "security-session-context-key";

    public static final String EASY4J_SECURITY_CONTEXT_SESSIONINFO_KEY = PARAM_PREFIX + StringPool.DOT + "security-context-key-session";
    public static final String EASY4J_SECURITY_CONTEXT_USERINFO_KEY = PARAM_PREFIX + StringPool.DOT + "security-context-key-userinfo";
    public static final String EASY4J_SECURITY_CONTEXT_AUTHORITY_KEY = PARAM_PREFIX + StringPool.DOT + "security-context-key-authority";

}
