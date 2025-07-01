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

import easy4j.infra.common.module.ModuleBoolean;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.api.cache.RedisEasy4jCache;
import easy4j.infra.context.api.idempotent.Easy4jIdempotentStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@Configuration(proxyBeanMethods = false)
@ModuleBoolean(SysConstant.EASY4J_REDIS_ENABLE)
public class Config {

    @Bean
    public CustomRedisson customRedisson() {
        return new CustomRedisson();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 设置键的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // 设置值的序列化方式（JSON格式）
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(JacksonUtil.getMapper()));

        // 设置Hash键的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置Hash值的序列化方式
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(JacksonUtil.getMapper()));

        template.afterPropertiesSet();
        return template;
    }

    @Bean("redisIdempotentStorage")
    public Easy4jIdempotentStorage redisIdempotentStorage() {
        return new RedisEasy4jIdempotentStorage();
    }


    @Bean
    RedisStartRunner redisStartRunner() {
        return new RedisStartRunner();
    }


    /**
     * redis缓存
     *
     * @author bokun.li
     * @date 2025/6/12
     */
    @Bean("redisEasy4jCache")
    public RedisEasy4jCache redisEasy4jCache(RedisTemplate<String, Object> redisTemplate) {
        return new RedisEasy4jCacheImpl("redis-easy4j-cache-impl", redisTemplate, Duration.ofMinutes(30L));
    }


    /**
     * redis 缓存配置 共用redisson连接池
     *
     * @author bokun.li
     * @date 2025/6/12
     */
//    @Bean(name = "redissonCacheManager")
//    @Primary
//    public CacheManager redissonCacheManager(RedissonClient redissonClient) {
//        Map<String, CacheConfig> cacheConfigMap = Maps.newHashMap();
//        CacheConfig cacheConfig = new CacheConfig();
//        cacheConfig.setTTL(Duration.ofMinutes(30).toMillis());
//        // 缓存区域
//        cacheConfigMap.put("easy4j", cacheConfig);
//
//        JsonJacksonCodec jsonJacksonCodec = new JsonJacksonCodec(JacksonUtil.getMapper(), true);
//        RedissonSpringCacheManager redissonSpringCacheManager = new RedissonSpringCacheManager(redissonClient, cacheConfigMap, jsonJacksonCodec);
//        // 事务提交完成之后放进去
//        redissonSpringCacheManager.setTransactionAware(true);
//        redissonSpringCacheManager.setAllowNullValues(false);
//        return redissonSpringCacheManager;
//    }
}
