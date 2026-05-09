package easy4j.module.redis;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.delay.DelayExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * redis 降级工厂
 *
 * @author bokun.li
 */
@Slf4j
public class RedisFallbackFactory {

    public final static Map<String, RedisCacheWithFallback<?>> FALLBACK_MAP = Maps.newConcurrentMap();


    public static <K> Optional<RedisCacheWithFallback<K>> registerAndGet(String key, Supplier<RedisCacheWithFallback<K>> fallback) {
        if (StrUtil.isBlank(key) && fallback != null) return Optional.ofNullable(fallback.get());
        if (fallback == null) return Optional.empty();
        RedisCacheWithFallback<K> redisCacheWithFallback = fallback.get();
        if (redisCacheWithFallback == null) return Optional.empty();
        FALLBACK_MAP.putIfAbsent(key, redisCacheWithFallback);
        RedisCacheWithFallback<K> redisCacheWithFallback1 = (RedisCacheWithFallback<K>) FALLBACK_MAP.get(key);
        return Optional.ofNullable(redisCacheWithFallback1);
    }

    public static <K> Optional<RedisCacheWithFallback<K>> getFallback(String key, Class<K> kc) {
        RedisCacheWithFallback<K> redisCacheWithFallback = (RedisCacheWithFallback<K>) FALLBACK_MAP.get(key);
        return Optional.ofNullable(redisCacheWithFallback);
    }

    /**
     * 通过降级函数的KEY拿取降级实例，然后调用降级实例中的清除key的方法
     *
     * @param fallbackKey 降级函数的KEY
     * @param key1        要删除的Key
     * @param key2        如果是hash结构则key2是key1对应的要删除的Key 如果key2要是为空且是hash结构则删除整个hash结构
     */
    public static void clear(String fallbackKey, String key1, String key2) {
        Optional<RedisCacheWithFallback> redisCacheWithFallback = Optional.ofNullable(FALLBACK_MAP.get(fallbackKey));
        if (redisCacheWithFallback
                .isPresent()) {
            boolean redisEnabled = redisCacheWithFallback.get().clearKey(key1, key2);
            if (redisEnabled) {
                DelayExecutor.instance.submit(ListTs.asList(fallbackKey, key1, key2), 1000L, (data) -> {
                    String s = ListTs.get(data, 0);
                    String s1 = ListTs.get(data, 1);
                    String s2 = ListTs.get(data, 2);
                    Optional.ofNullable(FALLBACK_MAP.get(s)).ifPresent(e2 -> e2.clearKey(s1, s2));
                });
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info(SysLog.compact("not found the fallbackKey instance 【" + fallbackKey + "】 so can't clear cache"));
            }
        }
    }

}
