package easy4j.module.lock.config;

import easy4j.module.base.context.ContextChannel;
import easy4j.module.base.context.Easy4jContext;
import easy4j.module.base.context.api.lock.DbLock;
import easy4j.module.base.context.api.lock.RedissonLock;
import easy4j.module.base.context.api.lock.ZkLock;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.lock.db.DbLockImpl;
import easy4j.module.lock.redisson.RedissonLockImpl;
import easy4j.module.lock.zk.ZkLockImpl;

public class LockContextChannel implements ContextChannel {
    public static Easy4jContext easy4jContext;

    @Override
    public <T> T listener(String type, String name, Class<T> aclass) {

        if (aclass == null) {
            return null;
        }

        if (DbLock.class.getName().equals(name)) {
            DbLockImpl dbLock = new DbLockImpl();
            return aclass.cast(dbLock);
        } else if (RedissonLock.class.getName().equals(name)) {
            RedissonLockImpl dbLock = new RedissonLockImpl();
            return aclass.cast(dbLock);
        } else if (ZkLock.class.getName().equals(name)) {
            ZkLockImpl dbLock = new ZkLockImpl();
            return aclass.cast(dbLock);
        }
        return null;
    }

    @Override
    public void init(Easy4jContext easy4jContext2) {
        easy4jContext = easy4jContext2;
        Easy4j.info("初始化LockContextChannel");
    }
}
