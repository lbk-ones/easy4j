package easy4j.module.redis;

import easy4j.module.base.plugin.idempotent.Easy4jIdempotentStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

// Redis分布式锁实现
@Component("redisIdempotentStorage")
@ConditionalOnBean(RedisTemplate.class)
public class RedisEasy4jIdempotentStorage implements Easy4jIdempotentStorage {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean acquireLock(String key, int expireSeconds) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", Duration.ofSeconds(expireSeconds)));
    }


    @Override
    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}