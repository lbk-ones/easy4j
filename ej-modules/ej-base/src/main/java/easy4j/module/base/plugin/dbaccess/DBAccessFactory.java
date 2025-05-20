package easy4j.module.base.plugin.dbaccess;


import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.utils.ListTs;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
@Slf4j
public class DBAccessFactory {
    public static final Set<String> INIT_DB_FILE_TYPE = new HashSet<>();
    public static final Set<String> INIT_DB_FILE_PATH = new HashSet<>();

    static {
        INIT_DB_FILE_PATH.add("db/log");
    }

    /**
     * get的时候顺带初始化
     *
     * @param dataSource
     * @param mixTransaction
     * @return
     */
    public static DBAccess getDBAccess(DataSource dataSource, boolean mixTransaction) {

        JdbcDbAccess jdbcDbAccess = new JdbcDbAccess();
        jdbcDbAccess.init(dataSource);
        jdbcDbAccess.setInTransaction(mixTransaction);
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
                try {
                    String databaseType = JdbcHelper.getDatabaseType(jdbcDbAccess.getConnection());
                    s1 = s + "/" + databaseType;
                    ClassPathResource classPathResource = new ClassPathResource(s1 + ".sql");
                    jdbcDbAccess.runScript(classPathResource);
                    log.info("the " + s1 + ".sql db initialization succeeded");
                } catch (Exception e) {
                    log.info("the " + s1 + ".sql db has been initialized");
                } finally {
                    INIT_DB_FILE_TYPE.add(s);
                }
            }
        }

    }
}
