package easy4j.infra.dbaccess.orm;

import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import jakarta.persistence.Access;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;

@Data
@Accessors(chain = true)
public class AccessConfig {

    // 数据源
    private DataSource dataSource;

    // 是否加入当前事务
    private boolean inTransaction = false;

    // 是否打印sql 默认打印
    private boolean printSqlIs = true;

    // 字段名称是否转下划线
    private boolean fieldNameToUnderline = true;

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

}
