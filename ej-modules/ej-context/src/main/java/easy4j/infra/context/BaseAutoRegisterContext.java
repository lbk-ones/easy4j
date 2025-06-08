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


import easy4j.infra.context.api.seed.DefaultEasy4jSeed;
import easy4j.infra.context.api.seed.Easy4jSeed;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-06-07 13:48:41
 */
public class BaseAutoRegisterContext implements AutoRegisterContext {

    // base module auto register
    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
//        easy4jContext.set(DbLog.class, DbLog.getDbLog());
        // give a default Easy4jSeed
        easy4jContext.set(Easy4jSeed.class, new DefaultEasy4jSeed());
    }
}
