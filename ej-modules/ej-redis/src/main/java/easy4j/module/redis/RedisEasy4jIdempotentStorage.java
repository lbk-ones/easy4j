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
