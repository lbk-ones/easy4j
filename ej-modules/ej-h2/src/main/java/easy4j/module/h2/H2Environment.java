package easy4j.module.h2;


import easy4j.module.base.annotations.Desc;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.resolve.DataSourceUrlResolve;
import easy4j.module.base.starter.AbstractEnvironmentForEj;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.starter.Easy4JStarterNd;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SysConstant;
import org.h2.Driver;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Objects;
import java.util.Properties;

@Order(value = 14)
public class H2Environment extends AbstractEnvironmentForEj {

    public static final String H2_SERVER_NAME = "EASY4j_H2_ENV_NAME";

    @Override
    public String getName() {
        return H2_SERVER_NAME;
    }

    @Desc("jdbc:h2:file:/path/to/your/h2db;DB_CLOSE_ON_EXIT=false 写入文件")
    @Override
    public Properties getProperties() {
        String dbType = getDbType();
        if ("other".equals(dbType)) {
            Properties properties = new Properties();
            EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
            boolean enableH2 = ejSysProperties.isH2Enable();
            if (enableH2) {
                String name = Driver.class.getName();
                String h2Url = ejSysProperties.getH2Url();

                properties.setProperty(SysConstant.DB_URL_DRIVER_CLASS_NAME, name);
                properties.setProperty(SysConstant.SPRING_H2_CONSOLE_ENABLED, "true");
                properties.setProperty(SysConstant.SPRING_H2_CONSOLE_PATH, "/h2-console");
                DataSourceUrlResolve dataSourceUrlResolve = new DataSourceUrlResolve();
                properties.setProperty(SysConstant.DB_USER_NAME, ejSysProperties.getH2ConsoleUsername());
                properties.setProperty(SysConstant.DB_USER_PASSWORD, ejSysProperties.getH2ConsolePassword());
                dataSourceUrlResolve.handler(properties,h2Url);

                return properties;
            }
        }
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
