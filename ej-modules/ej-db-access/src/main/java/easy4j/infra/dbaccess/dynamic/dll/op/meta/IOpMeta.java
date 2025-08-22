package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import java.sql.Connection;
import java.util.List;

/**
 * IOpMeta
 *
 * @author bokun.li
 * @date 2025-08-23
 */
public interface IOpMeta {

    void setConnection(Connection connection);

    /**
     * 主要版本号
     *
     * @return
     */
    int getMajorVersion();

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

    List<TableMetadata> getAllTableInfo();

    List<TableMetadata> getTableInfos(String tableNamePattern);


    List<DatabaseColumnMetadata> getColumns(String catLog, String schema, String tableName);

    List<PrimaryKeyMetadata> getPrimaryKes(String catLog, String schema, String tableName);

}
