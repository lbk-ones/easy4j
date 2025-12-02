package easy4j.infra.rpc.integrated;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 整合模块
 *
 * @author bokun
 * @since 2.0.1
 */
public class IntegratedFactory {

    private static final Map<Class<?>, Object> serverInstanceInitCache = new ConcurrentHashMap<>(128);

    public static void register(Object implObject) {
        if (null != implObject && !implObject.getClass().getName().equals(Object.class.getName())) {
            Class<?> aClass = implObject.getClass();
            if (
                    implObject instanceof ServerInstanceInit ||
                    implObject instanceof ConnectionManager
            ) {
                serverInstanceInitCache.put(aClass, implObject);
            }
        }
    }

    public static <T> T get(Class<T> tClass) {
        Object o = serverInstanceInitCache.get(tClass);
        if (o != null) {
            return (T) o;
        } else {
            throw new IllegalArgumentException("not support type" + tClass.getName());
        }
    }

    public static <T> T getOrDefault(Class<T> tClass, Supplier<? extends  T> function) {
        Object o = serverInstanceInitCache.get(tClass);
        if (o != null) {
            return (T) o;
        } else {
            T apply = function.get();
            if (null == apply) {
                throw new IllegalArgumentException("not support type" + tClass.getName());
            } else {
                return apply;
            }
        }
    }

}
