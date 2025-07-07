/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.datasource;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import easy4j.infra.base.resolve.DataSourceUrlResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
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
public class DataSourceEnvironment extends AbstractEasy4jEnvironment {

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
            // No data source was found! Please configure (easy4j.data-source-url), or check if the configuration center is accessible. If no database is required, please enable H2 in the startup annotation.
            throw new IllegalArgumentException(SysLog.compact("No data source was found! Please configure (" + SysConstant.DB_URL_STR_NEW + "), or check if the configuration center is accessible. If no database is required, please enable H2 in the startup annotation."));
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
            throw new IllegalArgumentException("无法解析数据库类型，请检查" + SysConstant.DB_URL_STR + "参数是否填写正确");
//            System.err.println();
//            System.exit(1);
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
