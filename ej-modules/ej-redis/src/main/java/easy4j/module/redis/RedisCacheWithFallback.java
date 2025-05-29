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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Redis缓存实现，带有数据库降级功能
 * 当Redis出现故障时，自动降级到数据库获取数据
 */
public class RedisCacheWithFallback<T> {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheWithFallback.class);
    private static final int MAX_FAILURES = 5; // 最大连续失败次数

    private static final long RECOVERY_TIME = 30_000; // 恢复时间（毫秒）

    private final RedisTemplate<String, T> redisTemplate;
    private final Function<String, T> databaseFallback;
    private final String cachePrefix;

    // 熔断状态
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private long lastFailureTime = 0;
    private final ReadWriteLock circuitLock = new ReentrantReadWriteLock();

    public RedisCacheWithFallback(RedisTemplate<String, T> redisTemplate,
                                  Function<String, T> databaseFallback,
                                  String cachePrefix) {
        this.redisTemplate = redisTemplate;
        this.databaseFallback = databaseFallback;
        this.cachePrefix = cachePrefix;
    }

    /**
     * 从缓存获取数据，如果Redis失败则从数据库获取
     */
    public T get(String key) {
        // 检查熔断状态
        if (isCircuitBroken()) {
            logger.warn("Redis circuit is broken, falling back to database");
            return getFromDatabase(key);
        }

        T value = null;
        try {
            // 添加Redis操作的监控
            StopWatch watch = new StopWatch();
            watch.start();

            // 执行Redis操作
            value = executeRedisOperation(key);

            watch.stop();
            logger.debug("Redis operation completed in {} ms", watch.getTotalTimeMillis());

            // 操作成功，重置失败计数
            resetFailureCount();

            // 如果缓存中没有数据，从数据库获取并更新缓存
            if (value == null) {
                logger.debug("Key {} not found in Redis, fetching from database", key);
                value = getFromDatabase(key);
                if (value != null) {
                    updateCache(key, value);
                }
            }
        } catch (Exception e) {
            // 记录失败并处理
            handleRedisFailure(key, e);
            // 从数据库获取
            value = getFromDatabase(key);
        }
        return value;
    }

    /**
     * 执行Redis操作
     */
    private T executeRedisOperation(String key) {
        String cacheKey = getCacheKey(key);
        return redisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * 从数据库获取数据
     */
    private T getFromDatabase(String key) {
        try {
            return databaseFallback.apply(key);
        } catch (Exception e) {
            logger.error("Database fallback failed for key: {}", key, e);
            throw new RuntimeException("Database operation failed", e);
        }
    }

    /**
     * 更新缓存
     */
    public void updateCache(String key, T value) {
        // 如果熔断打开，不尝试更新缓存
        if (isCircuitBroken()) {
            logger.debug("Circuit is broken, skipping cache update for key: {}", key);
            return;
        }

        try {
            String cacheKey = getCacheKey(key);
            redisTemplate.opsForValue().set(cacheKey, value);
            logger.debug("Cache updated for key: {}", key);
        } catch (Exception e) {
            handleRedisFailure(key, e);
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        if (isCircuitBroken()) {
            return;
        }

        try {
            String cacheKey = getCacheKey(key);
            redisTemplate.delete(cacheKey);
            logger.debug("Cache deleted for key: {}", key);
        } catch (Exception e) {
            handleRedisFailure(key, e);
        }
    }

    /**
     * 处理Redis失败
     */
    private void handleRedisFailure(String key, Exception e) {
        circuitLock.writeLock().lock();
        try {
            int failures = failureCount.incrementAndGet();
            lastFailureTime = System.currentTimeMillis();
            logger.error("Redis operation failed for key: {}, failure count: {}", key, failures, e);

            // 检查是否需要打开熔断
            if (failures >= MAX_FAILURES) {
                logger.error("Redis circuit breaker opened due to {} consecutive failures", failures);
            }
        } finally {
            circuitLock.writeLock().unlock();
        }
    }

    /**
     * 重置失败计数
     */
    private void resetFailureCount() {
        circuitLock.writeLock().lock();
        try {
            failureCount.set(0);
        } finally {
            circuitLock.writeLock().unlock();
        }
    }

    /**
     * 检查熔断是否打开
     */
    private boolean isCircuitBroken() {
        circuitLock.readLock().lock();
        try {
            int failures = failureCount.get();
            if (failures < MAX_FAILURES) {
                return false;
            }

            // 检查是否过了恢复时间
            long elapsedTime = System.currentTimeMillis() - lastFailureTime;
            if (elapsedTime >= RECOVERY_TIME) {
                // 尝试半开状态，重置失败计数
                resetFailureCount();
                return false;
            }

            return true;
        } finally {
            circuitLock.readLock().unlock();
        }
    }

    /**
     * 获取带前缀的缓存键
     */
    private String getCacheKey(String key) {
        return cachePrefix + ":" + key;
    }

    /**
     * 检查Redis连接是否正常
     */
    public boolean isRedisAvailable() {
        try {
            return Objects.equals("PONG", redisTemplate.execute(RedisConnection::ping));
        } catch (DataAccessException e) {
            logger.error("Redis connection check failed", e);
            return false;
        }
    }
}
