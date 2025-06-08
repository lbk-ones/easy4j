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
package easy4j.module.dubbo3;

import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * Dubbo3Environment
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Dubbo3Environment extends AbstractEasy4jEnvironment {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Properties getProperties() {

        Properties properties = new Properties();
        properties.setProperty("", "");
        properties.setProperty("", "");
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
