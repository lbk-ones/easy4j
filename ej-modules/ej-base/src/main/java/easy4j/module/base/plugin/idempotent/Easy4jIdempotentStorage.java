package easy4j.module.base.plugin.idempotent;

/**
 * Easy4jIdempotentStorage
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface Easy4jIdempotentStorage {
    boolean acquireLock(String key, int expireSeconds);
    void releaseLock(String key);
}