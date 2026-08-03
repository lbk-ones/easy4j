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
package easy4j.infra.dbaccess;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;
import easy4j.infra.common.utils.*;
import easy4j.infra.dbaccess.dll.op.DynamicDDL;
import easy4j.infra.dbaccess.helper.DDlHelper;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import easy4j.infra.dbaccess.orm.OrmFactory;
import easy4j.infra.dbaccess.orm.IDBAccess;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 模块内部使用的orm工具类，内部使用orm默认都为主数据源
 *
 * @author bokun.li
 * @date 2025-05
 */
@Setter
@Getter
@Slf4j
public class OrmInternal extends StandAbstractEasy4jResolve {
    public static final String CACHE_KEY = "easy4j-no-transaction-master-orm";
    public static final Set<SqlFileEnums> INITED_FILE_PATH = new HashSet<>();
    public static final Set<SqlFileEnums> INIT_DB_FILE_PATH = new HashSet<>();

    // 初始化的两个表，这两个表会默认创建
    static {
        INIT_DB_FILE_PATH.add(SqlFileEnums.DB_LOG);
        INIT_DB_FILE_PATH.add(SqlFileEnums.DB_SIMPLE_LOCK);
    }

    /**
     *
     * 获取不带事务,且不被全局属性影响的orm实例(只受spring中的printSqlIs属性的实时值影响)
     * datasource为获取当前Spring中的主数据源
     * 内部使用orm默认都为主数据源 所以直接缓存
     */
    public static IDBAccess getNoTransactionOrm() {
        if (ObjectHolder.INSTANCE.getObject(CACHE_KEY) == null) {
            IDBAccess idbAccess = getIdbAccess(null);
            ObjectHolder.INSTANCE.setObject(CACHE_KEY, idbAccess);
        }
        return (IDBAccess) ObjectHolder.INSTANCE.getObject(CACHE_KEY);
    }

    private static @NonNull IDBAccess getIdbAccess(DataSource dataSource) {
        IDBAccess idbAccess = OrmFactory.getInternal(dataSource, e -> {
            e.setInTransaction(false);
            e.setPrintSqlIs(true);
            // 全部sql都打印
            e.setOnlyPrintSlowSql(false);
        });
        init(idbAccess);
        return idbAccess;
    }

    public static void initDb(SqlFileEnums path) {
        INIT_DB_FILE_PATH.add(path);
        getNoTransactionOrm();
    }

    /**
     * flyway迁移前最后一次执行
     *
     * @param dataSource 传入数据源
     */
    public static void exeAll(DataSource dataSource) {
        List<SqlFileSpi> load = ServiceLoaderUtils.load(SqlFileSpi.class);
        for (SqlFileSpi sqlFileSpi : load) {
            List<SqlFileEnums> collect = sqlFileSpi.collect();
            if (CollUtil.isNotEmpty(collect)) {
                INIT_DB_FILE_PATH.addAll(collect);
            }
        }
        // 这里比较特殊，不使用缓存的 orm实例
        IDBAccess dbAccess = getIdbAccess(dataSource);
        init(dbAccess);
    }

    /**
     * 全局sql文件初始化的地方，已执行的sql不会再次执行
     *
     * @param jdbcDbAccess orm实例
     */
    public static void init(IDBAccess jdbcDbAccess) {
        synchronized (INIT_DB_FILE_PATH) {
            for (SqlFileEnums s : INIT_DB_FILE_PATH) {
                boolean contains = INITED_FILE_PATH.contains(s);
                if (contains) {
                    continue;
                }
                String s1 = s.getPath();
                Class<?> autoDDLClass = s.getAutoDDLClass();
                if (StrUtil.isBlank(s1) && autoDDLClass != null) {
                    autoDDL(autoDDLClass);
                    continue;
                }
                Connection connection = null;
                try {
                    connection = jdbcDbAccess.getConnection();
                    String databaseType = JdbcHelper.getDatabaseType(connection);
                    s1 = s1 + "/" + databaseType + SP.DOT + "sql";
                    DDlHelper.execDDL(connection, null, ListTs.asList(s1), true);
//                    ClassPathResource classPathResource = new ClassPathResource(s1 + ".sql");
//                    jdbcDbAccess.runScript(classPathResource);
                    log.info(SysLog.compact("the " + s1 + " db initialization succeeded"));
                } catch (Exception e) {
                    log.info(SysLog.compact("the " + s1 + " db has been initialized"));
                } finally {
                    JdbcHelper.close(connection);
                    INITED_FILE_PATH.add(s);
                }
            }
        }

    }

    public static TempDataSource getTempDataSource() {
        String normalDbUrl = getNormalDbUrl();
        String url = getUrl(normalDbUrl);
        String username = getUsername(normalDbUrl);
        String password = getPassword(normalDbUrl);
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(url);
        return new TempDataSource(driverClassNameByUrl, url, username, password);
    }

    /**
     * auto DDL
     *
     * @author bokun.li
     * @date 2025/9/18
     */
    public static void autoDDL(Class<?> aclass) {
        try {
            long begin = System.currentTimeMillis();
            DataSource tempDataSource = getTempDataSource();
            String s = null;
            try (DynamicDDL dynamicDDL = new DynamicDDL(tempDataSource, null, aclass)) {
                s = dynamicDDL.autoDDLByJavaClass(true);
            }
            long end = System.currentTimeMillis();
            log.info("autoDDL {} total cost {} ms,segment is {}", aclass.getName(), (end - begin), ListTs.splitToStr(s, SP.NEWLINE, SP.SPACE));
        } catch (Exception e) {
            log.error("autoDDL appear error" + e.getMessage());
        }
    }
}
