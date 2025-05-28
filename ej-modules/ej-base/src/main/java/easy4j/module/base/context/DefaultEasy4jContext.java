package easy4j.module.base.context;

import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DefaultEasy4jContext implements Easy4jContext {

    private static final ThreadLocal<Map<String, Map<String, Object>>> contextThreadLocal = new TransmittableThreadLocal<>();

    private static final Map<Class<?>, Object> contextSingleton = Maps.newConcurrentMap();
    private static final Map<String, Object> contextSingletonNameMap = Maps.newConcurrentMap();

    private DefaultEasy4jContext() {
    }

    private static class ContextHolder {
        private static final Easy4jContext INSTANCE = new DefaultEasy4jContext();
    }

    public static Easy4jContext getContext() {
        return ContextHolder.INSTANCE;
    }

    @Override
    public void registerThreadHash(String key, String key2, Object value) {
        Map<String, Map<String, Object>> contextMap = contextThreadLocal.get();
        if (null == contextMap) {
            contextThreadLocal.set(new HashMap<>());
        }
        if (StrUtil.isNotBlank(key2)) {
            contextMap = contextThreadLocal.get();
            Map<String, Object> stringObjectMap = contextMap.computeIfAbsent(key, (e) -> new HashMap<>());
            stringObjectMap.put(key2, value);
        }
    }

    @Override
    public Optional<Object> getThreadHashValue(String key, String key2) {
        return Optional.ofNullable(contextThreadLocal.get()).map(e -> e.get(key)).map(e -> e.get(key2));
    }

    @Override
    public Optional<Object> getThreadHash(String key) {
        return Optional.ofNullable(contextThreadLocal.get()).map(e -> e.get(key));
    }

    @Override
    public void clearHash() {
        contextThreadLocal.remove();
    }

    @Override
    public void registerSingleton(Class<?> aclass, Object t) {
        contextSingleton.putIfAbsent(aclass, t);
    }

    @Override
    public <T> T getSingleton(Class<T> aclass) {
        Object o = contextSingleton.get(aclass);
        if (null == o) {
            return null;
        }
        return aclass.cast(o);
    }

    @Override
    public void registerSingleton(String name, Object t) {
        contextSingletonNameMap.putIfAbsent(name, t);
    }

    @Override
    public <T> T getSingleton(String name, Class<T> aclass) {
        Object o = contextSingletonNameMap.get(name);
        if (null != o) {
            return aclass.cast(o);
        }
        return null;
    }
}
