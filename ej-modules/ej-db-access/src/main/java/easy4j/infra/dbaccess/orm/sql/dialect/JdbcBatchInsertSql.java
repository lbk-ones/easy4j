package easy4j.infra.dbaccess.orm.sql.dialect;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.orm.AccessField;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.runner.LogResult;
import easy4j.infra.dbaccess.orm.runner.LogSql;
import easy4j.infra.dbaccess.orm.runner.PsRes;
import easy4j.infra.dbaccess.orm.runner.StatementUtils;
import easy4j.infra.dbaccess.orm.sql.InsertSql;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * jdbc batch 模式
 */
@Slf4j
public class JdbcBatchInsertSql extends AbstractSqlDialect {

    /**
     * 用一下单条写入逻辑
     */
    private final InsertSql insertSql = new InsertSql();

    @Override
    public boolean match(RuntimeContext<?> context) {
        OperateType operateType = context.getOperateType();
        return operateType == OperateType.INSERT && context.isBatchIs();
    }

    @Override
    public String build(RuntimeContext<?> context) {
        return insertSql.build(context);
    }

    @Override
    public PsRes prepareStatementAndExe(RuntimeContext<?> runtimeContext) {
        String sql = runtimeContext.getSql();
        int batchSize = runtimeContext.getBatchSize();
        List<Object> args = runtimeContext.getArgs();
        PsRes psRes = new PsRes();
        int insertRows = runtimeContext.getInsertRows(args);
        List<List<Object>> partitionGroup = ListTs.splitCollection(args, insertRows);
        Connection conn = runtimeContext.getConnection();
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            int count = 0;
            // 会在后面统一关闭
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            psRes.setStatement(preparedStatement);
            LogSql.exeBegin(runtimeContext);
            int allEffectRows = 0;
            for (int i = 0; i < insertRows; i++) {
                List<Object> objects = partitionGroup.get(i);
                StatementUtils.fillParams(runtimeContext, preparedStatement, objects.toArray(new Object[]{}));
                preparedStatement.addBatch();
                count++;
                // 每100条执行一次批处理
                if (count % batchSize == 0) {
                    int[] ints = preparedStatement.executeBatch();
                    preparedStatement.clearBatch();
                    LogSql.exeEnd(runtimeContext);
                    LogSql.printBath(runtimeContext, sql, ints.length);
                    allEffectRows += ints.length;
                    if (count < insertRows) {
                        // 说明还有剩下的要执行 所以要为下一次的执行初始化开始时间
                        LogSql.exeBegin(runtimeContext);
                    }
                }
            }
            // 处理剩余数据
            if (count % batchSize != 0) {
                int[] ints = preparedStatement.executeBatch();
                preparedStatement.clearBatch();
                LogSql.exeEnd(runtimeContext);
                LogSql.printBath(runtimeContext, sql, ints.length);
                allEffectRows += ints.length;
            }
            // 提交数据
            conn.commit();
            psRes.setEffectRows(allEffectRows);
        } catch (SQLException e) {
            try {
                // 回滚数据
                conn.rollback();
            } catch (SQLException ex) {
                log.error("batch exception rollback",AccessUtils.translate("rollback", sql, ex, runtimeContext.getConfig().getDataSource()));
            }
            throw AccessUtils.translate("batch insert", sql, e, runtimeContext.getConfig().getDataSource());
        } finally {
            try {
                // 改为原来的模式
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                log.error("batch exception ",AccessUtils.translate("setAutoCommit", sql, e, runtimeContext.getConfig().getDataSource()));
            }
        }
        return psRes;
    }
}
