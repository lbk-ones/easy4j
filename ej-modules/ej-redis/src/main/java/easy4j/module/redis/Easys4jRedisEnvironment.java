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
package easy4j.module.redis;

import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.resolve.RedisPropertiesResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;
import java.util.Properties;

/**
 * Easys4jRedisEnvironment
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class Easys4jRedisEnvironment extends AbstractEasy4jEnvironment {

    public static final String REDIS_ENV_NAME = SysConstant.PARAM_PREFIX + StringPool.DOT + "redis.env.name";

    @Override
    public String getName() {
        return REDIS_ENV_NAME;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();
        RedisPropertiesResolve redisPropertiesResolve = new RedisPropertiesResolve();
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        Map<String, Object> beanMap = ejSysProperties.getBeanMap();
        redisPropertiesResolve.handler(properties, beanMap);
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        Easy4j.info(SysLog.compact("redis module has been init success!"));
    }
}
