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
package easy4j.module.mybatisplus;

import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 参数覆盖
 *
 * @author bokun.li
 * @date 2023/11/18
 */
public class MybatisPlusEnvironmentProperties extends AbstractEasy4jEnvironment {

    public static final String P_NAME = "easy4j-mybatis-plus";

    @Override
    public String getName() {
        return P_NAME;
    }

    /**
     * mapperLocations
     *
     * @return
     */
    public Properties getProperties() {
        // 关闭一级缓存 和二级缓存 （单服务可以不用关闭一级缓存）
        Properties properties = new Properties();
        properties.setProperty("mybatis-plus.configuration.cache-enabled", "false");
        properties.setProperty("mybatis-plus.configuration.localCacheScope", "STATEMENT");
        properties.setProperty("mybatis-plus.global-config.banner", "false");
        return properties;
    }


    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
