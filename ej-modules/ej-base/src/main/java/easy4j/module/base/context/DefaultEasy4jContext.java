package easy4j.module.base.context;

import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Maps;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.utils.BusCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 全局上下文注入
 * webmvc 全局本地线程注入
 */
public final class DefaultEasy4jContext implements Easy4jContext {

    public static final String DEFAULT_KEY = "default";

    private static final ThreadLocal<Map<String, Map<String, Object>>> contextThreadLocal = new TransmittableThreadLocal<>();

    private static final Map<String, Map<String, Object>> contextSingletonNameMap = Maps.newConcurrentMap();

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
    public <T, R extends T> void set(Class<T> aclass, R t) {
        set(aclass.getName(), t);
    }

    @Override
    public <T> T get(Class<T> aclass) {
        String name = aclass.getName();

        return get(name, aclass);
    }

    @Override
    public void set(String name, Object t) {
        setWith(DEFAULT_KEY, name, t);
    }

    private static void setWith(String type, String name, Object t) {
        contextSingletonNameMap
                .computeIfAbsent(type, k -> new HashMap<>())
                .put(name, t);
    }

    @Override
    public <T> T get(String name, Class<T> aclass) {
        return getT(DEFAULT_KEY, name, aclass);
    }

    private static <T> T getT(String type, String name, Class<T> aclass) {
        return Optional.ofNullable(contextSingletonNameMap.get(type))
                .map(e -> e.get(name))
                .map(aclass::cast)
                .orElseThrow(() -> EasyException.wrap(BusCode.A000031, "context is not find:" + aclass.getName() + "impl class"));
    }

    @Override
    public void setType(String type, Class<?> aclass, Object t) {
        if (StrUtil.isNotBlank(type) && Objects.nonNull(aclass) && Objects.nonNull(t)) {
            setWith(type, aclass.getName(), t);
        }
    }

    @Override
    public void setType(String type, String name, Object t) {
        if (StrUtil.isNotBlank(type) && StrUtil.isNotBlank(name) && Objects.nonNull(t)) {
            setWith(type, name, t);
        }
    }

    @Override
    public <T> T getType(String type, Class<T> aclass) {
        return getT(type, aclass.getName(), aclass);
    }

    @Override
    public <T> T getType(String type, String name, Class<T> t) {
        return getT(type, name, t);
    }
}
