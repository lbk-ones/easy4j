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
package easy4j.infra.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import easy4j.infra.common.module.Module;
import easy4j.infra.common.utils.SysConstant;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Config
 * 缓存配置
 *
 * @author bokun.li
 * @date 2025/6/12
 */
@EnableCaching
@Configuration
public class Config {


    @Bean("redisCacheManager")
    @DependsOn("redissonConnectionFactory")
    @Primary
    @Module(SysConstant.EASY4J_REDIS_ENABLE)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 自定义缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 为特定缓存区域设置不同的过期时间
        cacheConfigurations.put(SysConstant.PARAM_PREFIX, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(cacheConfigurations)
                .cacheDefaults(defaultConfig)
                .build();
    }

    // Caffeine 缓存管理器
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 配置 Caffeine 缓存
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());

        // 设置缓存区域
        cacheManager.setCacheNames(Arrays.asList("localCache", "sessionCache"));

        return cacheManager;
    }

    // 自定义缓存解析器，根据缓存名称选择缓存管理器
//    @Bean
//    public CacheResolver cacheResolver(CacheManager redisCacheManager) {
//        return new CustomCacheResolver(
//                redisCacheManager,
//                caffeineCacheManager()
//        );
//    }

//    @Bean
//    @DependsOn("redissonCacheManager")
//    public RedisEasy4jCache redisEasy4jCache(@Qualifier("redissonCacheManager") CacheManager redisCacheManager) {
//        return new RedisEasy4jCacheImpl(redisCacheManager);
//    }

}
