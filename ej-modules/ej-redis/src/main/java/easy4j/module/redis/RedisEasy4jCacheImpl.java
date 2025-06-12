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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY StringIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.context.AutoRegisterContext;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.cache.RedisEasy4jCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RedisEasy4jCacheImpl
 * Redis缓存实现
 *
 * @author bokun.li
 * @date 2025/6/12
 */
public class RedisEasy4jCacheImpl extends RedisEasy4jCache implements AutoRegisterContext {

    private final String name;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration defaultExpiration;
    private final Map<String, Duration> keyExpirations = new ConcurrentHashMap<>();
    private final DefaultCacheStats stats = new DefaultCacheStats();

    public RedisEasy4jCacheImpl(String name, RedisTemplate<String, Object> redisTemplate) {
        this(name, redisTemplate, null);
    }

    public RedisEasy4jCacheImpl(String name, RedisTemplate<String, Object> redisTemplate, Duration defaultExpiration) {
        this.name = name;
        this.redisTemplate = redisTemplate;
        this.defaultExpiration = defaultExpiration;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object get(String key) {
        stats.incrementRequestCount();
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Object value = ops.get(key);
        if (value != null) {
            stats.incrementHitCount();
        } else {
            stats.incrementMissCount();
        }
        return value;
    }

    @Override
    public Optional<Object> getOptional(String key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public Object get(String key, Callable<Object> valueLoader) {
        Object value = get(key);
        if (value != null) {
            return value;
        }

        try {
            long startTime = System.nanoTime();
            value = valueLoader.call();
            long loadTime = System.nanoTime() - startTime;
            stats.recordLoadTime(loadTime);

            if (value != null) {
                put(key, value);
            }
            return value;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load value for key: " + key, e);
        }
    }

    @Override
    public Object getOrDefault(String key, Object defaultValue) {
        Object value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public Map<String, Object> getAll(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        stats.incrementRequestCount(keys.size());
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        List<Object> values = ops.multiGet(new HashSet<>(keys));

        if (CollUtil.isEmpty(values)) {
            return Collections.emptyMap();
        }

        int hitCount = 0;
        Map<String, Object> result = new HashMap<>();
        Iterator<String> keyIterator = keys.iterator();
        Iterator<Object> valueIterator = values.iterator();

        while (keyIterator.hasNext() && valueIterator.hasNext()) {
            String key = keyIterator.next();
            Object value = valueIterator.next();
            if (value != null) {
                hitCount++;
                result.put(key, value);
            }
        }

        stats.incrementHitCount(hitCount);
        stats.incrementMissCount(keys.size() - hitCount);

        return result;
    }

    @Override
    public Map<String, Object> getAll(Collection<String> keys, Function<String, Object> valueMapper) {
        Map<String, Object> result = getAll(keys);

        // 查找缺失的键并通过valueMapper获取值
        Set<String> missingStringeys = new HashSet<>(keys);
        missingStringeys.removeAll(result.keySet());

        if (!missingStringeys.isEmpty()) {
            Map<String, Object> mappedValues = missingStringeys.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            valueMapper
                    ));
            result.putAll(mappedValues);

            // 将新获取的值存入缓存
            putAll(mappedValues);
        }

        return result;
    }

    @Override
    public void put(String key, Object value) {
        put(key, value, defaultExpiration);
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        if (value == null) {
            evict(key);
            return;
        }

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value);

        if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.expire(key, ttl);
            keyExpirations.put(key, ttl);
        }

        stats.incrementPutCount();
    }

    @Override
    public void putAll(Map<String, Object> entries) {
        putAll(entries, defaultExpiration);
    }

    @Override
    public void putAll(Map<String, Object> entries, Duration ttl) {
        if (entries == null || entries.isEmpty()) {
            return;
        }

        Map<String, Object> nonNullEntries = new HashMap<>();
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            if (entry.getValue() != null) {
                nonNullEntries.put(entry.getKey(), entry.getValue());
            }
        }

        if (!nonNullEntries.isEmpty()) {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.multiSet(nonNullEntries);

            if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
                for (String key : nonNullEntries.keySet()) {
                    redisTemplate.expire(key, ttl);
                    keyExpirations.put(key, ttl);
                }
            }

            stats.incrementPutCount(nonNullEntries.size());
        }
    }

    @Override
    public boolean putIfAbsent(String key, Object value) {
        return putIfAbsent(key, value, defaultExpiration);
    }

    @Override
    public boolean putIfAbsent(String key, Object value, Duration ttl) {
        if (value == null) {
            return false;
        }

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Boolean result = ops.setIfAbsent(key, value);

        if (Boolean.TRUE.equals(result) && ttl != null && !ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.expire(key, ttl);
            keyExpirations.put(key, ttl);
            stats.incrementPutCount();
        }

        return Boolean.TRUE.equals(result);
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
        keyExpirations.remove(key);
        stats.incrementEvictionCount();
    }

    @Override
    public void evictAll(Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(new HashSet<>(keys));
            keys.forEach(keyExpirations::remove);
            stats.incrementEvictionCount(keys.size());
        }
    }


    @Override
    public boolean containsKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public double getHitRate() {
        return stats.getHitRate();
    }

    @Override
    public CacheStats getStats() {
        return stats;
    }

    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
        RedisEasy4jCache bean = SpringUtil.getBean(RedisEasy4jCache.class);
        easy4jContext.register(bean);
    }

    /**
     * 默认缓存统计实现
     */
    private static class DefaultCacheStats implements CacheStats {

        // 获取缓存次数
        private final AtomicLong requestCount = new AtomicLong(0);
        // 缓存命中次数
        private final AtomicLong hitCount = new AtomicLong(0);
        // 缓存未命中
        private final AtomicLong missCount = new AtomicLong(0);
        // 当前缓存放入数量
        private final AtomicLong putCount = new AtomicLong(0);

        // 缓存过期时间
        private final AtomicLong evictionCount = new AtomicLong(0);

        // 缓存平均拿取时间
        private final AtomicLong totalLoadTime = new AtomicLong(0);

        // 缓存总加载个数
        private final AtomicLong loadCount = new AtomicLong(0);

        @Override
        public long getRequestCount() {
            return requestCount.get();
        }

        @Override
        public long getHitCount() {
            return hitCount.get();
        }

        @Override
        public long getMissCount() {
            return missCount.get();
        }

        @Override
        public double getHitRate() {
            long reqs = requestCount.get();
            return (reqs > 0) ? (double) hitCount.get() / reqs : 0.0;
        }

        @Override
        public double getMissRate() {
            long reqs = requestCount.get();
            return (reqs > 0) ? (double) missCount.get() / reqs : 0.0;
        }

        @Override
        public long getEvictionCount() {
            return evictionCount.get();
        }

        @Override
        public long getPutCount() {
            return putCount.get();
        }

        @Override
        public Duration getAverageLoadPenalty() {
            long count = loadCount.get();
            return (count > 0) ?
                    Duration.ofNanos(totalLoadTime.get() / count) :
                    Duration.ZERO;
        }

        public void incrementRequestCount() {
            requestCount.incrementAndGet();
        }

        public void incrementRequestCount(long count) {
            requestCount.addAndGet(count);
        }

        public void incrementHitCount() {
            hitCount.incrementAndGet();
        }

        public void incrementHitCount(long count) {
            hitCount.addAndGet(count);
        }

        public void incrementMissCount() {
            missCount.incrementAndGet();
        }

        public void incrementMissCount(long count) {
            missCount.addAndGet(count);
        }

        public void incrementPutCount() {
            putCount.incrementAndGet();
        }

        public void incrementPutCount(long count) {
            putCount.addAndGet(count);
        }

        public void incrementEvictionCount() {
            evictionCount.incrementAndGet();
        }

        public void incrementEvictionCount(long count) {
            evictionCount.addAndGet(count);
        }

        public void recordLoadTime(long loadTime) {
            totalLoadTime.addAndGet(loadTime);
            loadCount.incrementAndGet();
        }
    }
}
