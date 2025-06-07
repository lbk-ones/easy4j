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
package easy4j.module.base.context.api.lock;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * RedissonLock
 *
 * @author bokun.li
 * @date 2025-06-07 14:51:40
 */
public interface RedissonLock extends Easy4jLock {

    /**
     * 抢不到锁就快速返回 默认非公平锁
     *
     * @param key          锁的键
     * @param expireSecond 好多秒之后过期
     * @throws easy4j.module.base.exception.EasyException 抢不到就抛出异常A00042
     */
    void lock(List<String> key, int expireSecond);

    /**
     * 抢不到锁就快速返回 可以选择是否公平锁
     *
     * @param key          锁的键
     * @param isFaire      是否是公平锁
     * @param expireSecond 好多秒之后过期
     */
    void lock(List<String> key, boolean isFaire, int expireSecond);

    /**
     * 直接锁业务 不用手动释放锁
     *
     * @param key          锁的建
     * @param isFaire      是否公平锁
     * @param expireSecond 锁过期时间
     * @param function     锁的方法
     * @param <R>
     * @return
     */
    <R> R lockSupplier(List<String> key, boolean isSpin, boolean isFaire, int expireSecond, Supplier<R> function);

    /**
     * 直接锁Consumer类型业务 不用手动释放锁
     *
     * @param key          锁的建
     * @param isSpin       是否自旋
     * @param isFaire      是否公平锁
     * @param expireSecond 锁过期时间
     * @param function     锁的方法
     * @param <T>          参数类型
     * @return
     */
    <T> void lockConsumer(List<String> key, boolean isSpin, boolean isFaire, int expireSecond, T params, Consumer<T> function);

    /**
     * 自定义锁
     * 可实现看门狗机制
     *
     * @param key        锁的键
     * @param isSpin     是否自旋
     * @param isFaire    是否是公平锁
     * @param expireTime 上锁时间
     */
    void tryLock(List<String> key, boolean isSpin, boolean isFaire, int expireTime);

    /**
     * 解锁
     *
     * @param key 解锁的key
     */
    void unlock(List<String> key);
}
