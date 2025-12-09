package easy4j.infra.rpc.integrated;


import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.integrated.config.DefaultRpcConfig;
import easy4j.infra.rpc.server.ServerMethodInvoke;

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
                            implObject instanceof ConnectionManager ||
                            implObject instanceof IRpcConfig
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
            for (Object value : serverInstanceInitCache.values()) {
                Class<?> aClass1 = value.getClass();
                if (tClass.isAssignableFrom(aClass1)) {
                    serverInstanceInitCache.putIfAbsent(tClass, value);
                    return (T) value;
                }
            }
            throw new IllegalArgumentException("not support type" + tClass.getName());
        }
    }

    public static <T> T getOrDefault(Class<T> tClass, Supplier<? extends T> function) {
        Object o = serverInstanceInitCache.get(tClass);
        if (o != null) {
            return (T) o;
        } else {
            for (Object value : serverInstanceInitCache.values()) {
                Class<?> aClass1 = value.getClass();
                if (tClass.isAssignableFrom(aClass1)) {
                    serverInstanceInitCache.putIfAbsent(tClass, value);
                    return (T) value;
                }
            }
            T apply = function.get();
            if (null == apply) {
                throw new IllegalArgumentException("not support type" + tClass.getName());
            } else {
                return apply;
            }
        }
    }

    /**
     * 从配置中心中获取
     *
     * @return easy4j.infra.rpc.integrated.IRpcConfig
     */
    public static IRpcConfig getRpcConfig() {
        return getOrDefault(IRpcConfig.class, () -> DefaultRpcConfig.INSTANCE);
    }

    /**
     * 获取系统配置
     *
     * @return easy4j.infra.rpc.config.E4jRpcConfig
     */
    public static E4jRpcConfig getConfig() {
        return getRpcConfig().getConfig();
    }

    /**
     * 获取连接管理器
     *
     * @return easy4j.infra.rpc.integrated.ConnectionManager
     */
    public static ConnectionManager getConnectionManager() {
        return IntegratedFactory.getOrDefault(ConnectionManager.class, () -> DefaultConnectionManager.INSTANCE);
    }

    /**
     * 获取实例获取器
     *
     * @return easy4j.infra.rpc.integrated.ServerInstanceInit
     */
    public static ServerInstanceInit getServerInstanceInit() {
        return IntegratedFactory.getOrDefault(ServerInstanceInit.class, () -> DefaultServerInstanceInit.INSTANCE);
    }

}
