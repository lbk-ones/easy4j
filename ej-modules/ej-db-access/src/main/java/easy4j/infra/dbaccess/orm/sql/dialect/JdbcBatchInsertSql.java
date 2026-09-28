package easy4j.infra.dbaccess.orm.sql.dialect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
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
import java.util.*;

/**
 * <pre>
 * jdbc batch insert 模式
 * oracle 正常来说不会进入这个规则 oracle {@link OracleBatchInsertSql} {@link OracleInsertSql}
 * 但是如果oracle调用 batchSave也是会进入这个规则的
 * ps: jdbc的batch模式是不会代入回写的
 * </pre>
 */
@Slf4j
public class JdbcBatchInsertSql extends AbstractSqlDialect {

    @Override
    public boolean match(RuntimeContext<?> context) {
        OperateType operateType = context.getOperateType();
        return operateType == OperateType.INSERT && context.isBatchIs();
    }

    @Override
    public String build(RuntimeContext<?> runtimeContext) {
        String dotTableName = runtimeContext.getDotTableName();
        StringBuilder sql = new StringBuilder("insert into " + dotTableName + SP.SPACE);
        List<AccessField> insertFieldsList = runtimeContext.getInsertFields();
        List<AccessField> insertFields = runtimeContext.getColumnInfoList(insertFieldsList);
        List<String> fields = new ArrayList<>();
        for (AccessField insertField : insertFields) {
            String escapeColumnName = insertField.getEscapeColumnName();
            fields.add(escapeColumnName);
        }
        if (!fields.isEmpty()) {
            sql.append("(").append(ListTs.join(SP.SPACE + SP.COMMA + SP.SPACE, fields)).append(")");
        }
        sql.append(SP.SPACE);
        sql.append("values");
        sql.append(SP.SPACE);
        List<String> valueList = new ArrayList<>();
        for (AccessField insertField : insertFields) {
            valueList.add(insertField.getPlaceHolder());
        }
        String join = ListTs.join(SP.COMMA, valueList);
        sql.append(SP.LEFT_BRACKET + SP.SPACE).append(join).append(SP.SPACE).append(SP.RIGHT_BRACKET);
        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            sql.append(SP.SPACE).append(lastSql);
        }
        return sql.toString();
    }

    @Override
    public PsRes prepareStatementAndExe(RuntimeContext<?> runtimeContext) {
        String sql = runtimeContext.getSql();
        int batchSize = runtimeContext.getBatchSize();
        List<Object> args = runtimeContext.getArgs();
        PsRes psRes;
        int insertRows = runtimeContext.getInsertRows(args);
        List<List<Object>> partitionGroup = ListTs.splitCollection(args, insertRows);
        Connection conn = runtimeContext.getConnection();
        try {
            Executor[] executors = {};
            LogSql.exeBegin(runtimeContext);
            for (int i = 0; i < insertRows; i++) {
                List<Object> objects = partitionGroup.get(i);
                executors = ArrayUtil.append(executors, pstmt -> StatementUtils.fillParams(runtimeContext, pstmt, objects.toArray(new Object[]{})));
            }
            psRes = ExecutorUtil.executeBatchWithTransaction(conn, sql, executors, batchSize, (count, batchEffectRows) -> {
                LogSql.exeEnd(runtimeContext);
                LogSql.printBath(runtimeContext, sql, batchEffectRows);
                if (count < insertRows && count > 0) {
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
