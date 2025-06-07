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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import easy4j.module.base.context.api.lock.RedissonLock;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.utils.BusCode;
import easy4j.module.base.utils.ListTs;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * RedissonLockImpl
 *
 * @author bokun.li
 * @date 2025-06-07 15:02:57
 */
public class RedissonLockImpl implements RedissonLock {

    public ThreadLocal<Deque<RLock>> queueLock = new TransmittableThreadLocal<>();

    private volatile RedissonClient redissonClient;

    public void putQueue(RLock rLock) {
        if (null == rLock) return;
        Deque<RLock> rLocks = queueLock.get();
        if (null == rLocks) {
            rLocks = new ConcurrentLinkedDeque<>();
            queueLock.set(rLocks);
        }
        rLocks.addLast(rLock);
    }

    public RLock getLastLock() {
        Deque<RLock> rLocks = queueLock.get();
        if (null != rLocks) {
            return rLocks.pollLast();
        }
        return null;
    }

    public RedissonClient get() {
        if (redissonClient == null) {
            synchronized (RedissonLockImpl.class) {
                if (redissonClient == null) {
                    redissonClient = SpringUtil.getBean(RedissonClient.class);
                }
            }
        }
        if (redissonClient == null) {
            throw new EasyException("redis is not enable!");
        }
        return redissonClient;
    }


    @Override
    public void lock(String key, Integer expire) {
        RLock lock = get().getLock(key);
        boolean b;
        try {
            b = lock.tryLock(-1, expire, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            b = false;
        }
        if (!b) {
            throw EasyException.wrap(BusCode.A00042, key);
        } else {
            putQueue(lock);
        }
    }

    @Override
    public void unLock(String key) {
        try {
            RLock lastLock = getLastLock();
            if (null != lastLock) {
                lastLock.unlock();
            }
        } finally {
            queueLock.remove();
        }

    }

    @Override
    public void lock(List<String> key, int expireSecond) {
        if (CollUtil.isEmpty(key)) return;
        RLock multiLock = multiLock(key, false);
        if (multiLock == null) return;
        boolean b;
        try {
            b = multiLock.tryLock(-1, expireSecond, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            b = false;
        }
        if (!b) {
            throw EasyException.wrap(BusCode.A00042, Arrays.toString(key.toArray(new String[]{})));
        } else {
            putQueue(multiLock);
        }
    }

    private RLock multiLock(List<String> key, boolean isFair) {
        RedissonClient redissonClient1 = get();
        List<RLock> objects = ListTs.newArrayList();
        for (String s : key) {
            if (StrUtil.isBlank(s)) continue;
            RLock lock;
            if (isFair) {
                lock = redissonClient1.getFairLock(s);
            } else {
                lock = redissonClient1.getLock(s);
            }
            objects.add(lock);
        }
        if (CollUtil.isEmpty(objects)) return null;
        if (objects.size() == 1) {
            return ListTs.get(objects, 0);
        }
        return redissonClient1.getMultiLock(objects.toArray(new RLock[]{}));
    }

    @Override
    public void lock(List<String> key, boolean isFaire, int expireSecond) {
        if (CollUtil.isEmpty(key)) return;

        RLock multiLock = multiLock(key, isFaire);
        if (null == multiLock) return;
        boolean b;
        try {
            b = multiLock.tryLock(-1, expireSecond, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            b = false;
        }
        if (!b) {
            throw EasyException.wrap(BusCode.A00042, Arrays.toString(key.toArray(new String[]{})));
        } else {
            putQueue(multiLock);
        }
    }

    @Override
    public <R> R lockSupplier(List<String> key, boolean isSpin, boolean isFaire, int expireSecond, Supplier<R> function) {
        RLock rLock = multiLock(key, isFaire);
        if (null == rLock) {
            return function.get();
        }
        boolean b = false;
        try {
            if (isSpin) {
                b = rLock.tryLock(50, expireSecond, TimeUnit.SECONDS);
            } else {
                b = rLock.tryLock(-1, expireSecond, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ignored) {
        }
        R r;
        if (b) {
            putQueue(rLock);
            try {
                r = function.get();
            } finally {
                unLock(null);
            }
        } else {
            throw EasyException.wrap(BusCode.A00042, Arrays.toString(key.toArray(new String[]{})));
        }
        return r;
    }

    @Override
    public <T> void lockConsumer(List<String> key, boolean isSpin, boolean isFaire, int expireSecond, T params, Consumer<T> function) {
        RLock rLock = multiLock(key, isFaire);
        if (null == rLock) {
            function.accept(params);
            return;
        }
        boolean b = false;
        try {
            if (isSpin) {
                b = rLock.tryLock(50, expireSecond, TimeUnit.SECONDS);
            } else {
                b = rLock.tryLock(-1, expireSecond, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ignored) {
        }
        if (b) {
            putQueue(rLock);
            try {
                function.accept(params);
            } finally {
                unLock(null);
            }
        } else {
            throw EasyException.wrap(BusCode.A00042, Arrays.toString(key.toArray(new String[]{})));
        }

    }

    @Override
    public void tryLock(List<String> key, boolean isSpin, boolean isFaire, int expireTime) {
        RLock rLock = multiLock(key, isFaire);
        if (null == rLock) {
            return;
        }
        boolean b = false;
        try {
            if (isSpin) {
                b = rLock.tryLock(50, expireTime, TimeUnit.SECONDS);
            } else {
                b = rLock.tryLock(-1, expireTime, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ignored) {
        }
        if (b) {
            putQueue(rLock);
        } else {
            throw EasyException.wrap(BusCode.A00042, Arrays.toString(key.toArray(new String[]{})));
        }
    }

    @Override
    public void unlock(List<String> key) {
        unLock(null);
    }
}