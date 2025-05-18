package easy4j.module.datasource;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import easy4j.module.base.starter.AbstractEnvironmentForEj;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.starter.Easy4JStarterNd;
import easy4j.module.base.starter.EnvironmentHolder;
import easy4j.module.base.utils.SP;
import easy4j.module.base.utils.SqlType;
import easy4j.module.base.utils.SysConstant;
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
        String driverClassName = SqlType.getDriverClassName(dbType);
        String validateSql = SqlType.getValidateSql(dbType);
        properties.setProperty("spring.datasource.druid.aop-patterns", EnvironmentHolder.mainClassPath + SysConstant.DOT + "dao.*");
        properties.setProperty("spring.datasource.druid.driver-class-name", driverClassName);
        String lowerCase = dbType.toLowerCase();
        properties.setProperty("spring.datasource.druid.filter.stat.db-type", lowerCase);
        properties.setProperty("spring.datasource.druid.filter.wall.db-type", lowerCase);
        properties.setProperty("spring.datasource.druid.filter.wall.config.select-all-column-allow", "true");
        properties.setProperty("spring.datasource.druid.validationQuery", validateSql);

        try {
            String ejDataSrouceUrl = getEjDataSrouceUrl();

            if (StrUtil.isNotBlank(ejDataSrouceUrl)) {
                String[] split = ejDataSrouceUrl.split(SP.AT);
                String s = split[1];
                String[] split1 = s.split(SP.COLON);
                String url = split[0];
                String userName = split1[0];
                String password = split1[1];
                //String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(url);
                properties.setProperty(SysConstant.DB_DATASOURCE_TYPE, DATA_SOURCE_CLASS.getName());
                properties.setProperty(SysConstant.DB_URL_DRIVER_CLASS_NAME, driverClassName);
                properties.setProperty(SysConstant.DB_URL_STR, url);
                properties.setProperty(SysConstant.DB_USER_NAME, userName);
                properties.setProperty(SysConstant.DB_USER_PASSWORD, password);
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
        String ejDataSrouceUrl = getProperty(SysConstant.DB_URL_STR_NEW);

        Class<?> mainClass = EnvironmentHolder.mainClass;

        // 配置文件没有配置数据源url，则从mainClass中获取
        if (Objects.nonNull(mainClass) && StrUtil.isBlank(ejDataSrouceUrl)) {
            Easy4JStarter annotation = mainClass.getAnnotation(Easy4JStarter.class);
            if (Objects.nonNull(annotation)) {
                ejDataSrouceUrl = annotation.ejDataSourceUrl();
            } else {
                Easy4JStarterNd annotation2 = mainClass.getAnnotation(Easy4JStarterNd.class);
                if (Objects.nonNull(annotation2)) {
                    ejDataSrouceUrl = annotation2.ejDataSourceUrl();
                }
            }
        }
        return ejDataSrouceUrl;
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
