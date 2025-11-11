package easy4j.infra.dbaccess.dialect.v2;

import easy4j.infra.dbaccess.dynamic.dll.op.meta.*;
import easy4j.infra.dbaccess.helper.JdbcHelper;

import jakarta.annotation.Nullable;

import java.sql.SQLException;
import java.util.List;

public interface SchemaMetaDialect {

    /**
     * 主要版本号
     *
     * @return
     */
    int getMajorVersion();

    /**
     * 获取数据库类型
     *
     * @return
     * @see JdbcHelper#getDefaultDatabaseTypeMappings()
     */
    String getDbType();

    /**
     * 次要版本号
     *
     * @return
     */
    int getMinorVersion();

    /**
     * 版本号 可能有版本的详细描述信息
     *
     * @return
     */
    String getProductVersion();

    /**
     * 获取所有 表/视图 结构
     *
     * @return
     */
    List<TableMetadata> getAllTableInfo();

    /**
     * 根据tableType来查询表 TABLE/VIEW
     *
     * @param tableNamePattern 可以为空
     * @param tableType        表类型 说明：典型的类型包括"TABLE"（表）、"VIEW"（视图）、"SYSTEM TABLE"（系统表）、 "GLOBAL TEMPORARY"（全局临时表）、"LOCAL TEMPORARY"（本地临时表）、 "ALIAS"（别名）、"SYNONYM"（同义词）等
     * @return
     */
    List<TableMetadata> getAllTableInfoByTableType(@Nullable String tableNamePattern, String[] tableType);

    List<TableMetadata> getAllTableInfoByTableTypeNoCache(@Nullable String tableNamePattern, String[] tableType);

    /**
     * 根据表名称获取 表/视图 信息
     *
     * @param tableNamePattern
     * @return
     */
    List<TableMetadata> getTableInfos(String tableNamePattern);

    /**
     * 根据表名称获取 表/视图 中的字段信息
     *
     * @author bokun.li
     * @date 2025/8/23
     */
    List<DatabaseColumnMetadata> getColumns(String catLog, String schema, String tableName);

    /**
     * 查字段信息，不查缓存，直查
     *
     * @param catLog
     * @param schema
     * @param tableName
     * @return
     * @throws SQLException
     */
    List<DatabaseColumnMetadata> getColumnsNoCache(String catLog, String schema, String tableName) throws SQLException;

    /**
     * 查字段信息，不查缓存，直查
     *
     * @param catLog
     * @param schema
     * @param tableName
     * @return
     * @throws SQLException
     */
    List<DatabaseColumnMetadata> getColumnsNoCacheQuiet(String catLog, String schema, String tableName);

    /**
     * 根据表名称获取 表/视图 中的主键信息
     *
     * @author bokun.li
     * @date 2025/8/23
     */
    List<PrimaryKeyMetadata> getPrimaryKes(String catLog, String schema, String tableName);

    /**
     * 获取表索引信息
     *
     * @param catLog
     * @param schema
     * @param tableName
     * @return
     */
    List<IndexInfoMetaInfo> getIndexInfos(String catLog, String schema, String tableName);

    /**
     * 获取所有的数据库，有些数据库能否完全获取所有数据库，取决于获取连接的用户权限
     *
     * @return
     */
    List<CatalogMetadata> getCataLogs();

    /**
     * 获取当前数据库的schema信息，如果catlog为空代表查询所有的
     *
     * @return
     */
    List<SchemaMetadata> getSchemas(String catLog);

    /**
     * SchemaMetaDialect里面的方法 执行完毕之后是否关闭连接
     *
     * @param flag
     */
    void setCloseConnection(boolean flag);

}
