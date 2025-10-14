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
     * jdbc类型的更新，传入多条则批量更新，批量更新是一条一条更新和batch有本质区别
     *
     * @param record         传入要写入的数据map
     * @param tableName      表名
     * @param schema         schema
     * @param toUnderLine    将参数转为下划线（set后面的参数和where后面的参数都会转成下划线）
     * @param skipUpdateNull 跳过更新null值
     * @param isCommit       是否直接提交事务
     * @param whereFields    where条件中的额字段（可以带下划线，也可以不带，如果不带会受toUnderLine影响）
     * @return easy4j.infra.dbaccess.dialect.v2.PsResult
     */
    PsResult jdbcUpdate(List<Map<String, Object>> record, String tableName, String schema, boolean toUnderLine, boolean skipUpdateNull, boolean isCommit, List<String> whereFields);


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
