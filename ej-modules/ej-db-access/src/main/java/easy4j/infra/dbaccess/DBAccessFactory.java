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
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;
import easy4j.infra.common.utils.*;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;
import easy4j.infra.dbaccess.helper.DDlHelper;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DBAccessFactory
 *
 * @author bokun.li
 * @date 2025-05
 */
@Setter
@Getter
@Slf4j
public class DBAccessFactory extends StandAbstractEasy4jResolve {
    public static final Set<SqlFileEnums> INITED_FILE_PATH = new HashSet<>();
    public static final Set<SqlFileEnums> INIT_DB_FILE_PATH = new HashSet<>();

    static {
        INIT_DB_FILE_PATH.add(SqlFileEnums.DB_LOG);
        INIT_DB_FILE_PATH.add(SqlFileEnums.DB_SIMPLE_LOCK);
    }

    /**
     * get的时候顺带初始化
     *
     * @param dataSource
     * @param mixTransaction
     * @return
     */
    public static DBAccess getDBAccess(DataSource dataSource, boolean mixTransaction, boolean isPrintLog) {

        JdbcDbAccess jdbcDbAccess = new JdbcDbAccess();
        jdbcDbAccess.init(dataSource);
        jdbcDbAccess.setInTransaction(mixTransaction);
        jdbcDbAccess.setPrintLog(isPrintLog);
        init(jdbcDbAccess);
        return jdbcDbAccess;
    }

    /**
     * 这个也是一样顺带初始化
     *
     * @param dataSource
     * @return
     */
    public static DBAccess getDBAccess(DataSource dataSource) {
        JdbcDbAccess jdbcDbAccess = new JdbcDbAccess();
        jdbcDbAccess.init(dataSource);
        // 默认打印日志
        jdbcDbAccess.setPrintLog(true);
        init(jdbcDbAccess);
        return jdbcDbAccess;
    }

    public static void initDb(SqlFileEnums path) {
        INIT_DB_FILE_PATH.add(path);
        DataSource dataSource = SpringUtil.getBean(DataSource.class);
        getDBAccess(dataSource);
    }

    /**
     * 迁移前最后一次执行
     * @param dataSource 传入数据源
     */
    public static void exeAll(DataSource dataSource){
        List<SqlFileSpi> load = ServiceLoaderUtils.load(SqlFileSpi.class);
        for (SqlFileSpi sqlFileSpi : load) {
            List<SqlFileEnums> collect = sqlFileSpi.collect();
            if(CollUtil.isNotEmpty(collect)){
                INIT_DB_FILE_PATH.addAll(collect);
            }
        }
        DBAccess dbAccess = getDBAccess(dataSource);
        init(dbAccess);
    }

    /**
     * 全局sql文件初始化的地方，已执行的sql不会再次执行
     *
     * @param jdbcDbAccess
     */
    public static void init(DBAccess jdbcDbAccess) {
        synchronized (INIT_DB_FILE_PATH) {
            for (SqlFileEnums s : INIT_DB_FILE_PATH) {
                boolean contains = INITED_FILE_PATH.contains(s);
                if (contains) {
                    continue;
                }
                String s1 = s.getPath();
                Class<?> autoDDLClass = s.getAutoDDLClass();
                if(StrUtil.isBlank(s1) && autoDDLClass!=null){
                    autoDDL(autoDDLClass);
                    continue;
                }
                Connection connection = null;
                try {
                    connection = jdbcDbAccess.getConnection();
                    String databaseType = JdbcHelper.getDatabaseType(connection);
                    s1 = s1+ "/" + databaseType + SP.DOT + "sql";
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
