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

import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.starter.Easy4j;
import org.redisson.config.*;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;

/**
 * 更改redis连接池配置
 *
 * @author bokun.li
 * @date 2025-06-07 23:00:17
 */
public class CustomRedisson implements RedissonAutoConfigurationCustomizer {

    @Override
    public void customize(Config config) {

        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        //
        boolean dev = ejSysProperties.isDev();
        // default 30
        int redisMinIdeSize = ejSysProperties.getRedisMinIdeSize();
        // default 500
        int redisConnectionPoolSize = ejSysProperties.getRedisConnectionPoolSize();
        if (dev) {
            if (config.isSingleConfig()) {
                SingleServerConfig baseConfig = config.useSingleServer();
                baseConfig.setConnectionMinimumIdleSize(1);
                // 空闲连接超越最大空闲连接数 过期多少秒移除 milliseconds
                baseConfig.setIdleConnectionTimeout(1);
                // default is 10000
                baseConfig.setConnectTimeout(2000);
            } else if (config.isClusterConfig()) {
                ClusterServersConfig clusterServersConfig = config.useClusterServers();
                clusterServersConfig.setMasterConnectionMinimumIdleSize(1);
                clusterServersConfig.setSlaveConnectionMinimumIdleSize(1);
                clusterServersConfig.setIdleConnectionTimeout(1);
                // default is 10000
                clusterServersConfig.setConnectTimeout(2000);
            } else if (config.isSentinelConfig()) {
                SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
                sentinelServersConfig.setMasterConnectionMinimumIdleSize(1);
                sentinelServersConfig.setSlaveConnectionMinimumIdleSize(1);
                sentinelServersConfig.setIdleConnectionTimeout(1);
                // default is 10000
                sentinelServersConfig.setConnectTimeout(2000);
            }
        } else {
            if (config.isSingleConfig()) {
                SingleServerConfig baseConfig = config.useSingleServer();
                baseConfig.setConnectionMinimumIdleSize(redisMinIdeSize);
                baseConfig.setConnectionPoolSize(redisConnectionPoolSize);
            } else if (config.isClusterConfig()) {
                ClusterServersConfig clusterServersConfig = config.useClusterServers();
                clusterServersConfig.setMasterConnectionMinimumIdleSize(redisMinIdeSize);
                clusterServersConfig.setSlaveConnectionMinimumIdleSize(redisMinIdeSize);
                clusterServersConfig.setMasterConnectionPoolSize(redisConnectionPoolSize);
                clusterServersConfig.setSlaveConnectionPoolSize(redisConnectionPoolSize);
            } else if (config.isSentinelConfig()) {
                SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
                sentinelServersConfig.setMasterConnectionMinimumIdleSize(redisMinIdeSize);
                sentinelServersConfig.setSlaveConnectionMinimumIdleSize(redisMinIdeSize);
                sentinelServersConfig.setMasterConnectionPoolSize(redisConnectionPoolSize);
                sentinelServersConfig.setSlaveConnectionPoolSize(redisConnectionPoolSize);

            }
        }
    }
}
