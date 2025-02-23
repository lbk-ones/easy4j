package easy4j.module.base.plugin.idempotent;

public interface Easy4jIdempotentStorage {
    boolean acquireLock(String key, int expireSeconds);
    void releaseLock(String key);
}