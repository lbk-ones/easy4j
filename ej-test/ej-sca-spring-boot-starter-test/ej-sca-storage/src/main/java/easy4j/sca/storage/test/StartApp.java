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
package easy4j.sca.storage.test;


import easy4j.infra.base.starter.Easy4JStarter;

@Easy4JStarter(
        serverPort = 10002,
        serverName = "test-storage",
        serviceDesc = "测试",
        author = "bokun.li",
        enableH2 = false,
        h2Url = "jdbc:h2:file:~/h2/testdb;DB_CLOSE_ON_EXIT=false"
        // 使用h2当数据库
)
/**
 * StartApp
 *
 * @author bokun.li
 * @date 2025-05
 */
public class StartApp {
}
