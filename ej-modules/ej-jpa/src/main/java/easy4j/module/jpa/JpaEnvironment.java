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
package easy4j.module.jpa;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.common.utils.SysConstant;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.dialect.*;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.orm.hibernate5.SpringSessionContext;
import org.springframework.orm.jpa.vendor.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * spring.jpa.database=MYSQL
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=false
 * spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
 * spring.jpa.properties.hibernate.jdbc.batch_size=200
 * spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
 * spring.jpa.properties.hibernate.current_session_context_class=org.springframework.orm.hibernate5.SpringSessionContext
 * spring.jpa.properties.hibernate.hbm2ddl.auto =update
 */
@Order(value = 17)
public class JpaEnvironment extends AbstractEasy4jEnvironment {
    // 定义一个映射表，将 Database 枚举值映射到对应的 Hibernate 方言类名
    private static final Map<Database, String> DIALECT_MAP = new HashMap<>();

    static {
        // 初始化映射表
        DIALECT_MAP.put(Database.DB2, DB2Dialect.class.getName());
        DIALECT_MAP.put(Database.DERBY, DerbyDialect.class.getName());
        DIALECT_MAP.put(Database.H2, H2Dialect.class.getName());
        DIALECT_MAP.put(Database.HSQL, HSQLDialect.class.getName());
        //DIALECT_MAP.put(Database.INFORMIX, InformixDialect.class.getName());
        DIALECT_MAP.put(Database.MYSQL, MySQLDialect.class.getName());
        DIALECT_MAP.put(Database.ORACLE, OracleDialect.class.getName());
        DIALECT_MAP.put(Database.POSTGRESQL, PostgreSQLDialect.class.getName());
        DIALECT_MAP.put(Database.SQL_SERVER, SQLServerDialect.class.getName());
        DIALECT_MAP.put(Database.SYBASE, SybaseDialect.class.getName());
    }


    public static final String PROPERTIES_NAME = "jpa_env_properties";

    @Override
    public String getName() {
        return PROPERTIES_NAME;
    }

    @Override
    public Properties getProperties() {
        String dbType = getDbType();
        Database[] values = Database.values();
        String currDataBase = "";
        Database currDataBaseEnum = null;
        for (Database value : values) {
            String name = value.name();
            String replace = name.replace("_", "").toUpperCase();
            String upperCase = dbType.toUpperCase();
            if (StrUtil.equals(replace, upperCase)) {
                currDataBase = name;
                currDataBaseEnum = value;
                break;
            }
        }
        if (StrUtil.isBlank(currDataBase)) {
            System.err.println(dbType + "当前数据库不支持");
            System.exit(1);
            return null;
        }
        Properties properties = new Properties();
        properties.setProperty(SysConstant.SPRING_JPA_DATABASE, currDataBase);
        properties.setProperty(SysConstant.SPRING_JPA_SHOW_SQL, "true");
        properties.setProperty(SysConstant.SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL, "false");
        properties.setProperty(SysConstant.SPRING_JPA_PROPERTIES_HIBERNATE_USE_SQL_COMMENTS, "true");
        properties.setProperty(SysConstant.SPRING_JPA_HIBERNATE_NAMING_PHYSICAL_STRATEGY, PhysicalNamingStrategyStandardImpl.class.getName());
        properties.setProperty(SysConstant.SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE, "200");
        String s = DIALECT_MAP.get(currDataBaseEnum);
        properties.setProperty(SysConstant.SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT, s);
        properties.setProperty(SysConstant.SPRING_JPA_DATABASE_PLATFORM, s);
        // 与spring事务管理起来 保证事务准确 支持spring申明式事务
        properties.setProperty(SysConstant.SPRING_JPA_PROPERTIES_HIBERNATE_CURRENT_SESSION_CONTEXT_CLASS, SpringSessionContext.class.getName());
        // 表结构自动生成
        properties.setProperty(SysConstant.SPRING_JPA_PROPERTIES_HIBERNATE_HBM2DDL_AUTO, "update");
        properties.setProperty(SysConstant.SPRING_JPA_GENERATE_DDL, "true");
        properties.setProperty(SysConstant.SPRING_JPA_HIBERNATE_DDL_AUTO, "update");
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
