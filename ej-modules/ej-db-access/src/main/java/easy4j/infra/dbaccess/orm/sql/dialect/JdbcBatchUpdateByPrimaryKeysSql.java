package easy4j.infra.dbaccess.orm.sql.dialect;

import cn.hutool.core.util.ArrayUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessField;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.runner.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 * jdbc batch update 模式
 * ps:
 * </pre>
 */
@Slf4j
public class JdbcBatchUpdateByPrimaryKeysSql extends AbstractSqlDialect {

    @Override
    public boolean match(RuntimeContext<?> context) {
        OperateType operateType = context.getOperateType();
        return operateType == OperateType.UPDATE && context.isBatchIs();
    }

    // 最后的sql update TABLE set name1= ?, name2 = ? where id = ?
    @Override
    public String build(RuntimeContext<?> runtimeContext) {
        String whereSql = runtimeContext.getWhereSql();
        String s = "update " + runtimeContext.getDotTableName() + SP.SPACE + "set" + SP.SPACE + ListTs.join(SP.COMMA, runtimeContext.getSqlSet());
        // 这里有点特殊 不能in多个 所以多个的in变为单个的等于
        List<Object> whereArgs = runtimeContext.getWhereArgs();
        if (whereArgs.size() > 1) {
            List<AccessField> idList = runtimeContext.getIdList();
            String columnName = Objects.requireNonNull(ListTs.get(idList, 0)).getColumnName();
            s = runtimeContext.getAccessUtils().appendWhere(s, columnName + " = ?");
        } else {
            s = runtimeContext.getAccessUtils().appendWhere(s, whereSql);
        }
        return s;
    }

    @Override
    public PsRes prepareStatementAndExe(RuntimeContext<?> runtimeContext) {
        String sql = runtimeContext.getSql();
        int batchSize = runtimeContext.getBatchSize();
        List<Object> args = runtimeContext.getUpdateArgs();
        List<Object> whereArgs = runtimeContext.getWhereArgs();
        int updateRows = whereArgs.size();
        PsRes psRes;
        Connection conn = runtimeContext.getConnection();
        try {
            Executor[] executors = {};
            LogSql.exeBegin(runtimeContext);
            for (Object o : whereArgs) {
                List<Object> objects = new ArrayList<>(args);
                objects.add(o);
                executors = ArrayUtil.append(executors, pstmt -> {
                    StatementUtils.fillParams(runtimeContext, pstmt, objects.toArray(new Object[]{}));
                });
            }
            psRes = ExecutorUtil.executeBatchWithTransaction(conn, sql, executors, batchSize, (count, batchEffectRows) -> {
                LogSql.exeEnd(runtimeContext);
                LogSql.printBath(runtimeContext, sql, batchEffectRows);
                if (count < updateRows && count > 0) {
                    // 说明还有剩下的要执行 所以要为下一次的执行初始化开始时间
                    LogSql.exeBegin(runtimeContext);
                }
            });
        } catch (SQLException e) {
            throw AccessUtils.translate("batch insert", sql, e, runtimeContext.getConfig().getDataSource());
        }
        runtimeContext.setTempSkipPrintSql(true);
        return psRes;
    }
}
