package easy4j.module.base.context.api.lock;

public interface Easy4jLock {


    /**
     * 最基础的锁 只有过期时间 超时就不管
     */
    void lock(String key, Integer expire);


    /**
     * 解锁
     *
     * @param key
     */
    void unLock(String key);

}
