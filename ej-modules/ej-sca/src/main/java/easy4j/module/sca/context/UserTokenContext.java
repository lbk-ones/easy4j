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
package easy4j.module.sca.context;

/**
 * UserTokenContext
 *
 * @author bokun.li
 * @date 2025-05
 */
public class UserTokenContext {
    private static final ThreadLocal<String> userToken = new ThreadLocal<>();

    public UserTokenContext() {
    }

    public static String getToken() {
        return (String) userToken.get();
    }

    public static void setToken(String token) {
        userToken.set(token);
    }

    public static void remove() {
        userToken.remove();
    }
}
