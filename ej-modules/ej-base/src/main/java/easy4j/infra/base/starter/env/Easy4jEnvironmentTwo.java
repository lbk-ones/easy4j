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
package easy4j.infra.base.starter.env;

import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 环境初始化
 *
 * @author bokun.li
 * @date 2023/10/30
 */
@Order(value = 13)
public class Easy4jEnvironmentTwo extends AbstractEasy4jEnvironment {
    public static final String EASY4j_ENV_NAME = "easy4j-init-environment";

    @Override
    public String getName() {
        return EASY4j_ENV_NAME;
    }

    @Override
    public Properties getProperties() {
        return handlerDefaultAnnotationValues();
    }


    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        initEnv(environment, application);
        //String name = SystemUtil.getHostInfo().getName();
        //System.setProperty("LOG_FILE_NAME",this.getProperty("spring.application.name")+"-"+name.toLowerCase());
    }

}
