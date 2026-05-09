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

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.StopWatch;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Redis缓存实现，带有数据库降级功能
 * 当Redis出现故障时，自动降级到数据库获取数据
 * K: 数据库查询回调的传参类型
 *
 * @author bokun.li]
 */
public class RedisCacheWithFallback<K> {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheWithFallback.class);
    private static final int MAX_FAILURES = 5; // 最大连续失败次数

    private static final long RECOVERY_TIME = 30_000; // 恢复时间（毫秒）
    public static final String EMPTY_SUFFIX = "__empty__";
    private final RedisTemplate<String, Object> redisTemplate;
    private final Function<K, Object> databaseFallback;
    private final Function<K, String> getRedisKey;
    private final String cachePrefix;
    /**
     * 如果存hash结构的话 hashKey将由cachePrefix推算出来
     */
    private final Boolean isHash;

    // 熔断状态
    private static final AtomicInteger failureCount = new AtomicInteger(0);
    private long lastFailureTime = 0;
    private final ReadWriteLock circuitLock = new ReentrantReadWriteLock();

    // 默认五分钟
    private final Duration duration;

    private String getCachePrefix() {
        if (cachePrefix != null && !StrUtil.startWith(cachePrefix, SysConstant.PARAM_PREFIX + SP.COLON)) {
            return SysConstant.PARAM_PREFIX + SP.COLON + cachePrefix;
        }
        return cachePrefix;
    }

    private String getHashKey() {
        String nKey = cachePrefix;
        if (cachePrefix != null && !StrUtil.startWith(cachePrefix, SysConstant.PARAM_PREFIX + SP.COLON)) {
            nKey = SysConstant.PARAM_PREFIX + SP.COLON + cachePrefix;
        }
        if (StrUtil.endWith(nKey, SP.COLON)) {
            nKey = StrUtil.removeSuffix(nKey, SP.COMMA);
        }
        return nKey;
    }

    /**
     * @param redisTemplate    传入的 redisTemplate
     * @param databaseFallback 降级函数
     * @param cachePrefix      缓存键
     */
    public RedisCacheWithFallback(RedisTemplate<String, Object> redisTemplate,
                                  Function<K, Object> databaseFallback,
                                  Function<K, String> getRedisKey,
                                  String cachePrefix) {
        this.redisTemplate = redisTemplate;
        this.databaseFallback = ObjectUtil.defaultIfNull(databaseFallback, s -> null);
        this.cachePrefix = StrUtil.blankToDefault(cachePrefix, "");
        this.duration = Duration.ofMinutes(5L);
        this.isHash = false;
        this.getRedisKey = getRedisKey;
    }

    /**
     * @param redisTemplate    传入的 redisTemplate
     * @param databaseFallback 降级函数
     * @param cachePrefix      缓存键
     * @param duration         过期时间
     */
    public RedisCacheWithFallback(RedisTemplate<String, Object> redisTemplate,
                                  Function<K, Object> databaseFallback,
                                  Function<K, String> getRedisKey,
                                  String cachePrefix, Duration duration) {
        this.redisTemplate = redisTemplate;
        this.databaseFallback = ObjectUtil.defaultIfNull(databaseFallback, s -> null);
        this.cachePrefix = StrUtil.blankToDefault(cachePrefix, "");
        this.duration = duration;
        this.isHash = false;
        this.getRedisKey = getRedisKey;
    }

    /**
     * @param redisTemplate    传入的 redisTemplate
     * @param databaseFallback 降级函数
     * @param cachePrefix      缓存键
     * @param duration         过期时间
     * @param isHashKey        是否存成hash结构
     */
    public RedisCacheWithFallback(RedisTemplate<String, Object> redisTemplate,
                                  Function<K, Object> databaseFallback,
                                  Function<K, String> getRedisKey,
                                  String cachePrefix, Duration duration, Boolean isHashKey) {
        this.redisTemplate = redisTemplate;
        this.databaseFallback = ObjectUtil.defaultIfNull(databaseFallback, s -> null);
        this.cachePrefix = StrUtil.blankToDefault(cachePrefix, "");
        this.duration = duration;
        this.isHash = isHashKey;
        this.getRedisKey = getRedisKey;
    }

    /**
     * 从缓存获取数据，如果Redis失败则从数据库获取
     */
    public Object get(K data) {
        if (data == null) return null;
        // 检查熔断状态
        if (isCircuitBroken() || this.getRedisKey == null) {
            logger.warn("Redis circuit is broken, falling back to database");
            return getFromDatabase(data);
        }
        String redisKey = this.getRedisKey.apply(data);
        if (StrUtil.isBlank(redisKey)) return getFromDatabase(data);
        Object value = null;
        try {
            // 添加Redis操作的监控
            StopWatch watch = new StopWatch();
            watch.start();

            // 执行Redis操作
            value = executeRedisOperation(redisKey);

            watch.stop();
            logger.debug("Redis operation completed in {} ms", watch.getTotalTimeMillis());

            // 操作成功，重置失败计数
            resetFailureCount();

            // 如果缓存中没有数据，从数据库获取并更新缓存
            if (value == null) {
                logger.debug("Key {} not found in Redis, fetching from database", redisKey);
                value = getFromDatabase(data);
                if (value != null) {
                    updateCache(redisKey, value);
                }
            }
        } catch (Exception e) {
            // 记录失败并处理
            handleRedisFailure(redisKey, e);
            // 从数据库获取
            value = getFromDatabase(data);
        }
        return value;
    }

    /**
     * 执行Redis操作
     */
    private Object executeRedisOperation(String key) {
        String cacheKey = getCacheKey(key);
        if (redisTemplate == null) return null;
        Object res = null;
        if (!isHash) {
            res = redisTemplate.opsForValue().get(cacheKey);
            if (res == null) {
                res = redisTemplate.opsForValue().get(cacheKey + EMPTY_SUFFIX);
            }
        } else {
            HashOperations<String, String, Object> hp = redisTemplate.opsForHash();
            res = hp.get(getHashKey(), key);
            if (res == null) {
                res = hp.get(getHashKey() + EMPTY_SUFFIX, key);
            }
        }
        return res;
    }

    /**
     * 从数据库获取数据
     */
    private Object getFromDatabase(K data) {
        try {
            return databaseFallback.apply(data);
        } catch (Exception e) {
            logger.error("Database fallback failed for key: {}", data, e);
            throw new RuntimeException("Database operation failed", e);
        }
    }

    public static boolean isEmptyCollection(Object value) {
        // 1. 先判空
        if (value == null) {
            return true;
        }
        if (StrUtil.isBlankIfStr(value)) return true;

        // 2. 如果是集合（List/Set/Queue 等都继承自 Collection）
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }

        // 3. 如果是数组
        if (value.getClass().isArray()) {
            return Arrays.asList((Object[]) value).isEmpty();
        }

        // 都不是 → 不是空集合
        return false;
    }

    public void saveHashWithExpireLua(String key, String field, Object value, Long seconds) {
        String script = "redis.call('hset', KEYS[1], ARGV[1], ARGV[2]) " +
                "redis.call('expire', KEYS[1], ARGV[3]) " +
                "return 1";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        List<String> keys = Collections.singletonList(key);
        Long execute = redisTemplate.execute(redisScript, new StringRedisSerializer(), new RedisSerializer<>() {

            @Nullable
            @Override
            public byte[] serialize(@Nullable Long value) throws SerializationException {
                if (value == null) {
                    return new byte[0];
                }
                return ByteBuffer.allocate(8).putLong(value).array();
            }


            @Nullable
            @Override
            public Long deserialize(@Nullable byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length != 8) {
                    return null;
                }
                return ByteBuffer.wrap(bytes).getLong();
            }
        }, keys, field, JacksonUtil.toJson(value), String.valueOf(seconds));

        if (logger.isDebugEnabled()) {
            logger.debug("lua result is " + execute);
        }
    }

    /**
     * 更新缓存
     */
    public void updateCache(String key, Object value) {
        // 如果熔断打开，不尝试更新缓存
        if (isCircuitBroken()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Circuit is broken, skipping cache update for key: {}", key);
            }
            return;
        }
        if (redisTemplate == null) return;

        // 缓存空值，避免压垮数据库，查的时候查不到的时候再查一下redis，如果不为null就返回，就不用查数据库了
        boolean isEmpty = isEmptyCollection(value);

        // 为了防止疯狂写入 加一个500MS的锁
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(getCacheKey("lock:cache:write:" + key), "1", 500L, TimeUnit.MILLISECONDS);
        if (success) {
            try {
                if (isHash) {
                    HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
                    String hashKey = getHashKey();
                    if (isEmpty) {
                        String emptyKey = hashKey + EMPTY_SUFFIX;
                        saveHashWithExpireLua(emptyKey, key, value, 60L);
                    } else {
                        // 不永久缓存 缓存一天
                        saveHashWithExpireLua(hashKey, key, value, 60L * 60 * 24);
                    }
                } else {
                    String cacheKey = getCacheKey(key);
                    if (isEmpty) {
                        redisTemplate.opsForValue().set(cacheKey + EMPTY_SUFFIX, value, duration);
                    } else {
                        redisTemplate.opsForValue().set(cacheKey, value, duration);
                    }
                }
                logger.debug("Cache updated for key: {}", key);
            } catch (Exception e) {
                handleRedisFailure(key, e);
            }
        }

    }

    /**
     * 清除这个KEY
     *
     * @param key
     */
    public boolean clearKey(String key, String key2) {
        if (StrUtil.isBlank(key)) return false;
        // 如果熔断打开，不尝试更新缓存
        if (isCircuitBroken()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Circuit is broken, skipping cache clearKey for key: {}", key);
            }
            return false;
        }
        if (redisTemplate == null) return false;

        try {
            if (isHash) {
                String hashKey = getHashKey();
                if (StrUtil.isNotBlank(key2)) {
                    HashOperations<String, Object, Object> hp = redisTemplate.opsForHash();
                    hp.delete(hashKey, key2);
                    hp.delete(hashKey + EMPTY_SUFFIX, key2);
                } else {
                    redisTemplate.delete(hashKey);
                    redisTemplate.delete(hashKey + EMPTY_SUFFIX);
                }
            } else {
                ValueOperations<String, Object> hp = redisTemplate.opsForValue();
                String cacheKey = getCacheKey(key);
                hp.getAndDelete(cacheKey);
                hp.getAndDelete(cacheKey + EMPTY_SUFFIX);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("RedisCacheWithFallback clearKey error" + e.getMessage());
            }
            return false;
        }
        return true;
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
            if (redisTemplate == null) return;
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
            Boolean enable = Easy4j.getProperty(SysConstant.EASY4J_REDIS_ENABLE, Boolean.class);
            if (!enable) return true;
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
        return getCachePrefix().endsWith(":") ? getCachePrefix() : (getCachePrefix() + ":") + key;
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
        } catch (Exception e) {
            return false;
        }
    }
}
