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
package easy4j.infra.flyway;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import org.springframework.beans.factory.InitializingBean;

public class FlywayConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        boolean property1 = Easy4j.getProperty(SysConstant.EASY4J_FLYWAY_ENABLE, boolean.class);
        if (property1) {
            Easy4j.info(SysLog.compact("flyway init success...."));
            Easy4j.info(SysLog.compact("flyway path is " + Easy4j.getProperty("spring.flyway.locations")));
        } else {
            Easy4j.info(SysLog.compact("Flyway is disabled. If you are sure you want to enable it, please set " + SysConstant.EASY4J_FLYWAY_ENABLE + "=true"));
        }
    }
}
