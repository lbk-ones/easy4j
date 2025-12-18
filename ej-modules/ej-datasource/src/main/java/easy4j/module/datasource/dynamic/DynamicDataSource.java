package easy4j.module.datasource.dynamic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

/**
 * 继承 AbstractRoutingDataSource，重写数据源路由逻辑
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 核心方法：返回当前线程绑定的数据源标识，实现动态路由
     */
    @Override
    @Nullable
    protected Object determineCurrentLookupKey() {
        String dataSourceKey = DataSourceContextHolder.getDataSourceKey();
        if (log.isDebugEnabled()) {
            log.debug("【current thread " + Thread.currentThread().getId() + " 】change datasource to：" + dataSourceKey);
        }
        return dataSourceKey;
    }
}