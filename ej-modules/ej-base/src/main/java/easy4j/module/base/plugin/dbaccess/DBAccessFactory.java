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
package easy4j.module.base.plugin.dbaccess;


import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.utils.SysLog;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashSet;
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
public class DBAccessFactory {
    public static final Set<String> INIT_DB_FILE_TYPE = new HashSet<>();
    public static final Set<String> INIT_DB_FILE_PATH = new HashSet<>();

    static {
        INIT_DB_FILE_PATH.add("db/log");
        INIT_DB_FILE_PATH.add("db/simplelock");
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

    /**
     * 全局sql文件初始化的地方
     *
     * @param jdbcDbAccess
     */
    public static void init(DBAccess jdbcDbAccess) {
        synchronized (INIT_DB_FILE_PATH) {
            for (String s : INIT_DB_FILE_PATH) {
                boolean contains = INIT_DB_FILE_TYPE.contains(s);
                if (contains) {
                    continue;
                }
                String s1 = s;
                Connection connection = null;
                try {
                    connection = jdbcDbAccess.getConnection();
                    String databaseType = JdbcHelper.getDatabaseType(connection);
                    s1 = s + "/" + databaseType;
                    ClassPathResource classPathResource = new ClassPathResource(s1 + ".sql");
                    jdbcDbAccess.runScript(classPathResource);
                    log.info(SysLog.compact("the " + s1 + ".sql db initialization succeeded"));
                } catch (Exception e) {
                    log.info(SysLog.compact("the " + s1 + ".sql db has been initialized"));
                } finally {
                    JdbcHelper.close(connection);
                    INIT_DB_FILE_TYPE.add(s);
                }
            }
        }

    }
}
