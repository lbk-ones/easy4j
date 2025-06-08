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
package easy4j.module.sadmin.config;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.resolve.BootAdminPropertiesResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import jodd.util.StringPool;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * BootAdminConfig
 *
 * @author bokun.li
 * @date 2025-06-05 20:38:28
 */
public class BootAdminConfig extends AbstractEasy4jEnvironment {
    public static boolean isInit = false;

    public static final String ENV_NAME = SysConstant.PARAM_PREFIX + StringPool.DOT + "spring.boot.admin.env";

    @Override
    public String getName() {
        return ENV_NAME;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();
        String bootAdminServerUrl = getProperty(SysConstant.EASY4J_BOOT_ADMIN_SERVER_URL);
        BootAdminPropertiesResolve.get().handler(properties, bootAdminServerUrl);
        if (StrUtil.isNotBlank(bootAdminServerUrl)) {
            isInit = true;
        }
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        if (isInit) {
            Easy4j.info(SysLog.compact("boot-admin-client init successFull!"));
        }
    }
}