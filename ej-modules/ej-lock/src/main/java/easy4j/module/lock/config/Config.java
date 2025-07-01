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
package easy4j.module.lock.config;

import easy4j.infra.common.module.ModuleBoolean;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.api.lock.RedissonLock;
import easy4j.infra.context.api.lock.ZkLock;
import easy4j.module.lock.redisson.RedissonLockImpl;
import easy4j.module.lock.zk.ZkLockImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-06-07 15:00:29
 */
@Configuration(proxyBeanMethods = false)
public class Config {


//    @Bean
//    public DbLock dbLock() {
//        return new DbLockImpl();
//    }

    @Bean
    @ModuleBoolean(SysConstant.EASY4J_REDIS_ENABLE)
    public RedissonLock redissonLock() {
        return new RedissonLockImpl();
    }

    @Bean
    public ZkLock zkLock() {
        return new ZkLockImpl();
    }

}