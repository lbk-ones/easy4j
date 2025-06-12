package easy4j.infra.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Deprecated
//@Component
public class CustomCacheResolver implements CacheResolver {

    private final Map<String, CacheManager> cacheManagers = new HashMap<>();
    private final CacheManager defaultCacheManager;


    public CustomCacheResolver(CacheManager redisCacheManager, CacheManager caffeineCacheManager) {
        // 注册缓存管理器和对应的缓存区域
        cacheManagers.put("users", redisCacheManager);
        cacheManagers.put("products", redisCacheManager);
        cacheManagers.put("localCache", caffeineCacheManager);
        cacheManagers.put("sessionCache", caffeineCacheManager);

        // 设置默认缓存管理器
        this.defaultCacheManager = redisCacheManager;
    }

    @Override
    public Collection<org.springframework.cache.Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        String cacheName = context.getOperation().getCacheNames().iterator().next();
        CacheManager cacheManager = cacheManagers.getOrDefault(cacheName, defaultCacheManager);

        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        return Collections.singletonList(cache);
    }
}