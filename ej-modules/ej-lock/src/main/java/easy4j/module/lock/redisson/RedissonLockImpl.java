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
package easy4j.module.lock.redisson;

import easy4j.module.base.context.api.lock.RedissonLock;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * RedissonLockImpl
 *
 * @author bokun.li
 * @date 2025-06-07 15:02:57
 */
public class RedissonLockImpl implements RedissonLock {

    @Override
    public void lock(String key, Integer expire) {

    }

    @Override
    public void unLock(String key) {

    }

    @Override
    public boolean lock(List<String> key, int expireSecond) {
        return false;
    }

    @Override
    public boolean lock(List<String> key, boolean isFaire, int expireSecond) {
        return false;
    }

    @Override
    public <R> R lockSupplier(List<String> key, boolean isSpin, boolean isFaire, int expireSecond, Supplier<R> function) {
        return null;
    }

    @Override
    public <T> void lockConsumer(List<String> key, boolean isSpin, boolean isFaire, int expireSecond, T params, Consumer<T> function) {

    }

    @Override
    public boolean tryLock(List<String> key, boolean isSpin, boolean isFaire, int expireTime) {
        return false;
    }

    @Override
    public void unlock(List<String> key) {

    }
}