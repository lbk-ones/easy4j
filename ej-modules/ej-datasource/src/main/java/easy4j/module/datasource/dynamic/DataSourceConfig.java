package easy4j.module.datasource.dynamic;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import easy4j.infra.base.properties.DataSourceProperties;
import easy4j.infra.base.properties.DynamicDataSourceProperties;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <pre>
 * 动态数据源配置
 * 多数据源共存 核心是druid会根据spring的配置初始化一个主库，这个主库不动为默认库
 * 有个缺点 这个时候 ioc容器中 已经有两个 DataSource bean了 所以要强制将新的DataSource优先级调高
 * </pre>
 * @author bokun
 */
@Configuration
@ConditionalOnProperty(prefix = SysConstant.PARAM_PREFIX, name = "dynamic-data-source.enabled", havingValue = "true")
@AutoConfigureAfter({DruidDataSourceAutoConfigure.class})
@Slf4j
public class DataSourceConfig {

    /**
     * 动态创建所有数据源（根据配置的 Map 批量生成）
     */
    private Map<Object, Object> createAllDataSources() {
        Map<String, DataSourceProperties> datasources = Optional.ofNullable(Easy4j.getEjSysProperties())
                .map(EjSysProperties::getDynamicDataSource)
                .map(DynamicDataSourceProperties::getDatasource)
                .orElseGet(HashMap::new);
        Map<Object, Object> targetDataSources = new HashMap<>();
        Integer initialSize = Easy4j.getProperty("spring.datasource.druid.initial-size", Integer.class);
        Integer maxActiveSize = Easy4j.getProperty("spring.datasource.druid.max-active", Integer.class);
        Integer minIdle = Easy4j.getProperty("spring.datasource.druid.min-idle", Integer.class);
        Integer maxWait = Easy4j.getProperty("spring.datasource.druid.max-wait", Integer.class);
        // 遍历配置的数据源，逐个创建 Druid 数据源
        for (Map.Entry<String, DataSourceProperties> entry : datasources.entrySet()) {
            String dsKey = entry.getKey(); // 数据源标识（如 db1、db2）
            DataSourceProperties dsProps = entry.getValue(); // 数据源参数
            String url = dsProps.getUrl();
            String username = dsProps.getUsername();
            String password = dsProps.getPassword();
            if (StrUtil.hasEmpty(url, username, password, dsKey) || DataSourceContextHolder.DEFAULT_KEY.equals(dsKey)) {
                continue;
            }
            // 创建 Druid 数据源
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setName(dsKey + "DruidDataSource");
            druidDataSource.setUrl(url);
            druidDataSource.setUsername(username);
            druidDataSource.setPassword(password);
            String driverClassName = dsProps.getDriverClassName();
            if (StrUtil.isEmpty(driverClassName)) {
                driverClassName = SqlType.getDriverClassNameByUrl(url);
            }
            // 复用 Druid 通用配置（可根据需要自定义）
            druidDataSource.setDriverClassName(driverClassName);
            druidDataSource.setInitialSize(initialSize);
            druidDataSource.setMinIdle(minIdle);
            druidDataSource.setMaxActive(maxActiveSize);
            druidDataSource.setMaxWait(maxWait);
            String validateSqlByUrl = SqlType.getValidateSqlByUrl(url);
            druidDataSource.setValidationQuery(validateSqlByUrl);
            druidDataSource.setTestWhileIdle(true);
            targetDataSources.put(dsKey, druidDataSource);
        }
        return targetDataSources;
    }

//    @Bean
//    public DataSourceAspect dataSourceAspect() {
//        return new DataSourceAspect();
//    }

    /**
     * 核心：动态数据源 Bean
     * 如果业务类注入
     * DataSource dataSource;
     * 那么返回的一定是这个
     */
    @Primary
    @Qualifier("dataSource")
    @Bean("dynamicDataSource")
    public DataSource dynamicDataSource(DataSource dataSource) {
        log.info(SysLog.compact("enable dynamic datasource"));
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> targetDataSources = createAllDataSources();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(dataSource);
        dynamicDataSource.setLenientFallback(true);
        return dynamicDataSource;
    }

    /**
     * 拦截器
     */
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    public Advisor dynamicDatasourceAnnotationAdvisor() {
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(true);
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor, easy4j.module.datasource.dynamic.DataSource.class);
        advisor.setOrder(Integer.MIN_VALUE);
        return advisor;
    }

    /**
     * 事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dynamicDataSource(dataSource));
    }
}