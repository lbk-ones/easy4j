package easy4j.infra.dbaccess.orm;

import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Accessors(chain = true)
public class AccessConfig {

    private static final Map<DataSource, String> DB_MAP = new ConcurrentHashMap<>();

    // 数据源
    private DataSource dataSource;

    // 是否加入当前事务
    private boolean inTransaction = false;

    // 是否打印sql 默认打印
    private boolean printSqlIs = true;

    // 默认只打印慢sql
    private boolean onlyPrintSlowSql = true;

    // 多慢的sql算慢
    private long slowSqlTime = 1000L;

    // 字段名称是否转下划线
    private boolean fieldNameToUnderline = true;

    // oracle的字段是否自动大写 默认自动大写
    private boolean oracleAutoUpperCase = true;

    // db2的字段是否自动大写 默认自动大写
    private boolean db2AutoUpperCase = true;

    // postgresql的字段是否自动小写 默认自动小写
    private boolean pgAutoLowerCase = true;

    // h2的字段是否自动大写 默认自动小写
    private boolean h2AutoUpperCase = true;

    // oracle 写入策略 默认第一种
    // 1: 循环单条写入（带主键回写）
    // 2: insert into value () select x1,x2 from dual union all select x1,x2 from dual 这种写法不带回写
    private int oracleWriteStrategy = 1;


    private Map<String,PluginState> plugins = new ConcurrentHashMap<>();

    @Data
    public static class PluginState{

        private boolean enabled;
    }


    /**
     * 设置 JDBC 驱动从数据库服务端「单次批量拉取多少条结果集数据」，用来控制游标批量读取行数，优化大结果集内存占用与网络 IO。
     * <hr />
     * Mysql:
     * fetchSize = 0（默认）：一次性拉取全部结果集到客户端内存；
     * <hr />
     * fetchSize = Integer.MIN_VALUE：开启流式逐行读取（单行拉取），逐条从服务端拿数据，不缓存整表，适合超大表查询防 OOM；
     * <hr />
     * 设为普通正数（如 1000）不生效，驱动仍会一次性加载所有数
     * <hr />
     * Oracle / PostgreSQL:
     * 原生完整支持 fetchSize
     */
    private Integer fetchSize;

    public String getDbType() {
        DataSource dataSource1 = this.dataSource;
        if (dataSource1 != null) {
            String s = DB_MAP.get(dataSource1);
            if (s == null) {
                synchronized (DB_MAP) {
                    if (DB_MAP.get(dataSource1) == null) {
                        try (Connection connection = dataSource1.getConnection()) {
                            DialectV2 dialectV2 = DialectFactory.get(connection);
                            String dbType = dialectV2.getDbType();
                            DB_MAP.put(dataSource1, dbType);
                            s = dbType;
                        } catch (SQLException ignored) {
                        }
                    }
                }
            }
            return s;
        }
        return "";
    }

}
