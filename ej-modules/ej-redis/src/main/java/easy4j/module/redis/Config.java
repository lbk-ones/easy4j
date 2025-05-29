package easy4j.module.redis;

import easy4j.module.base.plugin.idempotent.Easy4jIdempotentStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Config {

    @Bean("redisIdempotentStorage")
    @ConditionalOnBean(RedisTemplate.class)
    public Easy4jIdempotentStorage redisIdempotentStorage(){
        return new RedisEasy4jIdempotentStorage();
    }

}