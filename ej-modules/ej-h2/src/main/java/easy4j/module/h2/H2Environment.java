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
package easy4j.module.h2;


import easy4j.infra.common.annotations.Desc;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.resolve.DataSourceUrlResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import org.h2.Driver;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * H2Environment
 *
 * @author bokun.li
 * @date 2025-05
 */
@Order(value = 14)
public class H2Environment extends AbstractEasy4jEnvironment {

    public static final String H2_SERVER_NAME = "EASY4j_H2_ENV_NAME";

    @Override
    public String getName() {
        return H2_SERVER_NAME;
    }

    @Desc("jdbc:h2:file:/path/to/your/h2db;DB_CLOSE_ON_EXIT=false 写入文件")
    @Override
    public Properties getProperties() {
        String dbType = getDbType();
        if ("other".equals(dbType)) {
            Properties properties = new Properties();
            EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
            boolean enableH2 = ejSysProperties.isH2Enable();
            if (enableH2) {
                String name = Driver.class.getName();
                String h2Url = ejSysProperties.getH2Url();

                properties.setProperty(SysConstant.DB_URL_DRIVER_CLASS_NAME, name);
                properties.setProperty(SysConstant.SPRING_H2_CONSOLE_ENABLED, "true");
                properties.setProperty(SysConstant.SPRING_H2_CONSOLE_PATH, "/h2-console");
                DataSourceUrlResolve dataSourceUrlResolve = new DataSourceUrlResolve();
                properties.setProperty(SysConstant.DB_USER_NAME, ejSysProperties.getH2ConsoleUsername());
                properties.setProperty(SysConstant.DB_USER_PASSWORD, ejSysProperties.getH2ConsolePassword());
                dataSourceUrlResolve.handler(properties, h2Url);

                return properties;
            }
        }
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
