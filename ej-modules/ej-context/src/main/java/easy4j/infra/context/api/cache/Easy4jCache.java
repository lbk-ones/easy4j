package easy4j.infra.context.api.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 缓存顶层抽象接口，定义了缓存操作的基本行为
 * 适用于内存缓存、分布式缓存等多种实现
 */
public interface Easy4jCache<K, V> {

    /**
     * 获取缓存名称
     */
    String getName();

    /**
     * 获取缓存中的值，如果不存在则返回null
     */
    V get(K key);

    /**
     * 获取缓存中的值，返回Optional对象
     */
    Optional<V> getOptional(K key);

    /**
     * 获取缓存中的值，如果不存在则通过valueLoader加载
     */
    V get(K key, Callable<V> valueLoader);

    /**
     * 获取缓存中的值，如果不存在则通过valueMapper映射
     */
    V getOrDefault(K key, V defaultValue);

    /**
     * 批量获取缓存中的值
     */
    Map<K, V> getAll(Collection<K> keys);

    /**
     * 批量获取缓存中的值，对于不存在的键使用valueMapper进行映射
     */
    Map<K, V> getAll(Collection<K> keys, Function<K, V> valueMapper);

    /**
     * 存入缓存，默认过期时间
     */
    void put(K key, V value);

    /**
     * 存入缓存，指定过期时间
     */
    void put(K key, V value, Duration ttl);

    /**
     * 批量存入缓存
     */
    void putAll(Map<K, V> entries);

    /**
     * 批量存入缓存，指定过期时间
     */
    void putAll(Map<K, V> entries, Duration ttl);

    /**
     * 如果不存在则存入缓存
     */
    boolean putIfAbsent(K key, V value);

    /**
     * 如果不存在则存入缓存，指定过期时间
     */
    boolean putIfAbsent(K key, V value, Duration ttl);

    /**
     * 删除缓存项
     */
    void evict(K key);

    /**
     * 批量删除缓存项
     */
    void evictAll(Collection<K> keys);

    /**
     * 判断缓存中是否存在指定键
     */
    boolean containsKey(K key);


    /**
     * 获取缓存命中率
     */
    double getHitRate();

    /**
     * 获取缓存统计信息
     */
    CacheStats getStats();

    /**
     * 缓存统计信息接口
     */
    interface CacheStats {
        // 获取请求次数 requestCount
        long getRequestCount();


        // 缓存命中数量
        long getHitCount();

        // 缓存未命中数量
        long getMissCount();

        // 命中率 HitCount / requestCount
        double getHitRate();

        // 未命中率 MissCount / requestCount
        double getMissRate();

        // 缓存过期key数量
        long getEvictionCount();

        // 获取缓存存入数量
        long getPutCount();

        // 获取缓存平均拿取数量
        Duration getAverageLoadPenalty();
    }
}
