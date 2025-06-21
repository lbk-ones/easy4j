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
package easy4j.infra.base.resolve;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;

import java.util.Map;

/**
 * Redis Properties Resolve
 * SINGLE单机连发：
 * 127.0.0.1:6379@username:password
 * username可以没有
 * <p>
 * SENTINEL连发(无username)
 * 127.0.0.1:6379,127.0.0.2:6379@mymaster:password
 * <p>
 * 集群模式的差不多也
 * 127.0.0.1:6379,127.0.0.2:6379@password
 *
 * @author bokun.li
 * @date 2025/6/6
 */
public class RedisPropertiesResolve extends ObjectMapStrObjectAbstractResovle {

    public static final String SINGLE = "Single";
    public static final String SENTINEL = "Sentinel";
    public static final String CLUSTER = "Cluster";

    @Override
    public Object handler(Object t, Map<String, Object> map) {
        String easy4jRedisUrl = Convert.toStr(map.get(SysConstant.EASY4J_REDIS_URL));
        if (StrUtil.isBlank(easy4jRedisUrl)) {
            return t;
        }
        String redisPassword = Easy4j.getProperty(map, SysConstant.SPRING_REDIS_PASSWORD, String.class);
        String easy4jRedisConnectionType = Easy4j.getProperty(map, SysConstant.EASY4J_REDIS_CONNECTION_TYPE, String.class);
        switch (easy4jRedisConnectionType) {
            case SINGLE:
                String host = getHost(easy4jRedisUrl);
                String port = getPort(easy4jRedisUrl);
                String password = getPassword(easy4jRedisUrl);
                setSpringProperty(t, SysConstant.SPRING_REDIS_HOST, host);
                setSpringProperty(t, SysConstant.SPRING_REDIS_PORT, port);
                setSpringProperty(t, SysConstant.SPRING_REDIS_PASSWORD, StrUtil.blankToDefault(password, redisPassword));
                break;
            case SENTINEL:
                String mymaster = getUsername(easy4jRedisUrl);
                String url1 = getUrl(easy4jRedisUrl);
                String password1 = getPassword(easy4jRedisUrl);
                setSpringProperty(t, SysConstant.SPRING_REDIS_SENTINEL_MASTER, mymaster);
                setSpringProperty(t, SysConstant.SPRING_REDIS_SENTINEL_NODES, url1);
                setSpringProperty(t, SysConstant.SPRING_REDIS_SENTINEL_PASSWORD, password1);
                setSpringProperty(t, SysConstant.SPRING_REDIS_PASSWORD, password1);
                break;
            case CLUSTER:
                String url2 = getUrl(easy4jRedisUrl);
                String password2 = getPassword(easy4jRedisUrl);
                setSpringProperty(t, SysConstant.SPRING_REDIS_CLUSTER_NODES, url2);
                setSpringProperty(t, SysConstant.SPRING_REDIS_PASSWORD, password2);
                setSpringProperty(t, SysConstant.SPRING_REDIS_CLUSTER_MAX_REDIRECTS, "5");
                break;
            default:
                throw new EasyException("redis connection type is not allow" + easy4jRedisConnectionType);
        }

        // 不使用 lettuce
        setSpringProperty(t, SysConstant.SPRING_REDIS_LETTUCE_POOL_ENABLE, "false");
        setSpringProperty(t, SysConstant.SPRING_REDIS_LETTUCE_POOL_MAX_ACTIVE, 1000);
        setSpringProperty(t, SysConstant.SPRING_REDIS_LETTUCE_POOL_MAX_IDLE, 200);
        setSpringProperty(t, SysConstant.SPRING_REDIS_LETTUCE_POOL_MIN_IDLE, 100);
        setSpringProperty(t, SysConstant.SPRING_REDIS_LETTUCE_POOL_SHUTDOWN_TIMEOUT, 1000);
        setSpringProperty(t, SysConstant.SPRING_REDIS_LETTUCE_POOL_MAX_WAIT, 5000);
        // read
        setSpringProperty(t, SysConstant.SPRING_REDIS_TIMEOUT, 1000);
        // connection
        setSpringProperty(t, SysConstant.SPRING_REDIS_CONNECT_TIMEOUT, 2000);
        setSpringProperty(t, SysConstant.EASY4J_REDIS_ENABLE, true);

        return t;
    }
}
