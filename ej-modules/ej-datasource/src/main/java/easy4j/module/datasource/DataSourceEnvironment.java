package easy4j.module.datasource;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import easy4j.module.base.resolve.DataSourceUrlResolve;
import easy4j.module.base.starter.AbstractEnvironmentForEj;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.starter.Easy4JStarterNd;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SP;
import easy4j.module.base.utils.SqlType;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * 数据源相关
 *
 * @author bokun.li
 * @date 2023/11/20
 */
@Order(value = 16)  // 要在 h2 后面加载
public class DataSourceEnvironment extends AbstractEnvironmentForEj {

    // 使用的是什么数据源
    public static final Class<? extends DataSource> DATA_SOURCE_CLASS = DruidDataSource.class;

    public static final String DS_NAME = "easy4j-datasource-environment";


    @Override
    public String getName() {
        return DS_NAME;
    }

    @Override
    public Properties getProperties() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("datasource.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String dbType = getDbType();
        if ("other".equals(dbType)) {
            System.err.println(SysLog.compact("未发现数据源！请配置(" + SysConstant.DB_URL_STR_NEW + ")，或者检查配置中心是否处于可访问状态，若不需要数据库，请在启动注解开启H2"));
            System.exit(1);
        }
        String driverClassName = SqlType.getDriverClassName(dbType);
        String validateSql = SqlType.getValidateSql(dbType);
        properties.setProperty("spring.datasource.druid.aop-patterns", Easy4j.mainClassPath + SysConstant.DOT + "dao.*");
        properties.setProperty("spring.datasource.druid.driver-class-name", driverClassName);
        String lowerCase = dbType.toLowerCase();
        properties.setProperty("spring.datasource.druid.filter.stat.db-type", lowerCase);
        properties.setProperty("spring.datasource.druid.filter.wall.db-type", lowerCase);
        properties.setProperty("spring.datasource.druid.filter.wall.config.select-all-column-allow", "true");
        properties.setProperty("spring.datasource.druid.validationQuery", validateSql);

        try {
            String ejDataSrouceUrl = getEjDataSrouceUrl();

            if (StrUtil.isNotBlank(ejDataSrouceUrl)) {
                //String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(url);
                properties.setProperty(SysConstant.DB_DATASOURCE_TYPE, DATA_SOURCE_CLASS.getName());
                properties.setProperty(SysConstant.DB_URL_DRIVER_CLASS_NAME, driverClassName);
                DataSourceUrlResolve dataSourceUrlResolve = new DataSourceUrlResolve();
                dataSourceUrlResolve.handler(properties, ejDataSrouceUrl);
                return properties;
            } else {

                getLogger().info("可以使用 " + SysConstant.DB_URL_STR_NEW + "=jdbc:xxx://xxx@username:password 简化数据库配置");
            }
        } catch (Exception e) {
            getLogger().error(SysConstant.DB_URL_STR_NEW + e.getMessage() + "格式为: " + SysConstant.DB_URL_STR_NEW + "=jdbc:xxx://xxx@username:password");
        }

        return properties;
    }

    private String getEjDataSrouceUrl() {
        return getProperty(SysConstant.DB_URL_STR_NEW);
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        String property = getProperty(SysConstant.DRUID_FILTER);
        String dbUtlStr = getDbUrl();
        String dbType = JdbcUtils.getDbType(dbUtlStr, null);
        DbType dbType1 = DbType.of(dbType);
        if (Objects.isNull(dbType1)) {
            System.err.println("无法解析数据库类型，请检查" + SysConstant.DB_URL_STR + "参数是否填写正确");
            System.exit(1);
            return;
        }
        if (StrUtil.contains(property, "log4j3")) {
            /// 日志相关
            System.setProperty("druid.log.conn", "false");
            // 参数日志
            System.setProperty("druid.log.stmt", "true");
            System.setProperty("druid.log.stmt.executableSql", "true");
            System.setProperty("druid.log.rs", "false");
            // 错误
            System.setProperty("druid.log.conn.logError", "true");
            System.setProperty("druid.log.stmt.logError", "true");
        }
    }
}
