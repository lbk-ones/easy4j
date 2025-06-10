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

import cn.hutool.system.SystemUtil;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.common.utils.SysConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class FlywayEnviroment extends AbstractEasy4jEnvironment {

    public static final String FLYWAY_ENV_NAME = SysConstant.PARAM_PREFIX + SP.DOT + "flyway-env-name";

    @Override
    public String getName() {
        return FLYWAY_ENV_NAME;
    }

    // 不用系统参数来简化 直接给默认值
    @Override
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("spring.flyway.locations", "classpath:db/migration");

        properties.setProperty("spring.flyway.schemas", "public");
        properties.setProperty("spring.flyway.table", "sys_flyway_schema_history");

        // 不允许无序执行
        properties.setProperty("spring.flyway.out-of-order", "false");

        // 禁用 flyway clean 命令
        properties.setProperty("spring.flyway.clean-disabled", "false");
        // 自动创建基线
        properties.setProperty("spring.flyway.baseline-on-migrate", "true");
        // 从0开始
        properties.setProperty("spring.flyway.baseline-version", "0");


        boolean enable = Easy4j.getProperty(SysConstant.EASY4J_FLYWAY_ENABLE, boolean.class);

        if (SystemUtil.getOsInfo().isLinux() || enable) {
            properties.setProperty(SysConstant.EASY4J_FLYWAY_ENABLE, "true");
            properties.setProperty("spring.flyway.enabled", "true");
        }
        String normalDbUrl = getNormalDbUrl();
        String url = getUrl(normalDbUrl);
        String username = getUsername(normalDbUrl);
        String password = getPassword(normalDbUrl);
        String dataTypeByUrl = SqlType.getDataTypeByUrl(url);
        DbType dbType = DbType.getDbType(dataTypeByUrl);
        String db = dbType.getDb();
        if ("other".equals(db)) {
            throw new EasyException("the db  " + url + "  is not support!");
        }
        String driverClassName = SqlType.getDriverClassName(db);
        properties.setProperty("spring.flyway.url", url);
        properties.setProperty("spring.flyway.driver-class-name", driverClassName);
        properties.setProperty("spring.flyway.user", username);
        properties.setProperty("spring.flyway.password", password);

        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
