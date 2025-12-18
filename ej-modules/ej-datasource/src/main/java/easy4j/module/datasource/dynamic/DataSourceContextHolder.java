package easy4j.module.datasource.dynamic;

import org.springframework.stereotype.Component;

/**
 * 数据源上下文（适配自定义数据源标识）
 */
@Component
public class DataSourceContextHolder {

    public static final String DEFAULT_KEY = "$easy4j_default";

    // 线程隔离的数据源标识存储
    private static final ThreadLocal<String> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    /**
     * 设置当前线程的数据源标识
     */
    public static void setDataSourceKey(String key) {
        CONTEXT_HOLDER.set(key);
    }

    /**
     * 获取当前线程的数据源标识（无则返回默认）
     */
    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get() == null ? DEFAULT_KEY : CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的数据源标识
     */
    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }
}