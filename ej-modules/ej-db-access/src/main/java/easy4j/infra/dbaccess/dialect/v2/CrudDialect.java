package easy4j.infra.dbaccess.dialect.v2;

import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.condition.WhereBuild;

import java.util.List;
import java.util.Map;

/**
 * crud相关的基础操作
 */
public interface CrudDialect {

    /**
     * jdbc类型的写入，传入多条则批量写入
     *
     * @param record      传入要写入的数据map
     * @param tableName   表名
     * @param schema      schema
     * @param batchSize   每次批量的大小
     * @param toUnderLine 将参数转为下划线
     * @param isCommit    是否直接提交事务
     * @return
     */
    PsResult jdbcInsert(List<Map<String, Object>> record, String tableName, String schema, int batchSize, boolean toUnderLine, boolean isCommit);

    /**
     * jdbc类型的更新，传入多条则批量更新
     *
     * @param record             传入要写入的数据map
     * @param tableName          表名
     * @param schema             schema
     * @param skipNotExistsField 跳过不存在的字段
     * @param toUnderLine        将参数转为下划线
     * @param skipUpdateNull     跳过更新null值
     * @param isCommit           是否直接提交事务
     * @param whereBuild         条件构造器
     * @return
     */
    PsResult jdbcUpdate(List<Map<String, Object>> record, String tableName, String schema, boolean skipNotExistsField, boolean toUnderLine, boolean skipUpdateNull, boolean isCommit, WhereBuild whereBuild);


    /**
     * jdbc类型的删除
     *
     * @param tableName   表名
     * @param schema      schema
     * @param toUnderLine 将参数转为下划线
     * @param isCommit    是否直接提交事务
     * @param whereBuild  条件构造器
     * @return
     */
    PsResult jdbcDelete(String tableName, String schema, boolean toUnderLine, boolean isCommit, WhereBuild whereBuild);


    /**
     * 根据分页对象获取分页sql语句
     *
     * @param sql  未分页sql语句
     * @param page 分页对象
     * @return
     */
    String getPageSql(String sql, Page<?> page);

}
