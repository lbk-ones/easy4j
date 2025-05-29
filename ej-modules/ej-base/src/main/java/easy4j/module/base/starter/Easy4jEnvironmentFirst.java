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
package easy4j.module.base.starter;

import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.utils.SysConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 先于spring配置加载 所以这个时候配置的手动拿取
 */
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER - 2)
public class Easy4jEnvironmentFirst extends AbstractEnvironmentForEj {

    public static final String SCA_PROPERTIES_NAME = "easy4j-environment-first";

    @Override
    public String getName() {
        return SCA_PROPERTIES_NAME;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        preLoadApplicationProfile();
    }
}
