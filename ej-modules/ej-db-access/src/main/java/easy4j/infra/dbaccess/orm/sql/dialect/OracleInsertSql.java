package easy4j.infra.dbaccess.orm.sql.dialect;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessField;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.runner.*;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * oracle 单条循环写入，带回写
 */
public class OracleInsertSql extends AbstractSqlDialect {

    @Override
    public boolean match(RuntimeContext<?> context) {
        int oracleWriteStrategy = context.getAccessUtils().getAccessConfig().getOracleWriteStrategy();
        return context.getOperateType() == OperateType.INSERT && Objects.equals(context.getDbType(), DbType.ORACLE.getDb()) && oracleWriteStrategy == 1;
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
        sql.append(SP.LEFT_BRACKET);
        for (int i = 0; i < insertFields.size(); i++) {
            AccessField accessField = insertFields.get(i);
            sql.append(SP.SPACE);
            if (i != 0) {
                sql.append(SP.COMMA);
            }
            sql.append(accessField.getPlaceHolder());
        }
        sql.append(SP.RIGHT_BRACKET);
        return sql.toString();
    }

    /**
     * oracle的批量写入很特殊，特别是要回写就更特殊了，所以只有这样将就一下
     *
     * @param runtimeContext the function argument
     * @return
     */
    @Override
    public PsRes prepareStatementAndExe(RuntimeContext<?> runtimeContext) {
        String sql = runtimeContext.getSql();
        List<Object> args = runtimeContext.getArgs();
        List<?> params = runtimeContext.getParams();
        List<AccessField> columnInfoList = runtimeContext.getColumnInfoList(runtimeContext.getInsertFields());
        int oneRowArgSize = columnInfoList.size();
        PsRes psRes = new PsRes();
        Connection conn = runtimeContext.getConnection();
        List<AccessField> autoIncrementList = runtimeContext.getAutoIncrementList();
        try {
            String[] array = autoIncrementList.stream().map(AccessField::getEscapeColumnName).toList().toArray(new String[]{});
            PreparedStatement cstmt = null;
            if (array.length > 0) {
                cstmt = conn.prepareStatement(sql, array);
            } else {
                // 这个时候回写的是 row_id
                cstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }
            psRes.setStatement(cstmt);
            int i2 = args.size() / oneRowArgSize;
            List<ResultSet> resultSets = new ArrayList<>();
            List<List<Object>> partitionGroup = ListTs.splitCollection(args, i2);
            List<Map<String, Object>> handle = new ArrayList<>();
            int effectRows = 0;
            for (int rowIndex = 0; rowIndex < i2; rowIndex++) {
                LogSql.exeBegin(runtimeContext);
                LogResult logResult = runtimeContext.getLogResult();
                // 这里重新计算开始时间
                logResult.setBeginTime(System.currentTimeMillis());
                // 一行的参数集合
                List<Object> objects = partitionGroup.get(rowIndex);
                StatementUtils.fillParams(runtimeContext, cstmt, objects.toArray(new Object[]{}));
                int i = cstmt.executeUpdate();
                effectRows += i;
                runtimeContext.setTempEffectRows(i);

                ResultSet generatedKeys = cstmt.getGeneratedKeys();
                resultSets.add(generatedKeys);
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle_ = mapListHandler.handle(generatedKeys);
                if (!handle_.isEmpty()) {
                    handle.addAll(handle_);
                } else {
                    // 塞个null进去对齐
                    handle.add(null);
                }
                LogSql.exeEnd(runtimeContext);

                runtimeContext.setTempPrintSqlArgs(objects);
                LogSql.print(runtimeContext);
            }
            runtimeContext.setTempPrintSqlArgs(null);
            runtimeContext.setTempEffectRows(0);
            if (i2 > 1) {
                runtimeContext.setTempSkipPrintSql(true);
            }
            JdbcUtils.writeBack(params, handle, autoIncrementList);
            psRes.setEffectRows(effectRows);
            psRes.setResultSets(resultSets);

        } catch (SQLException sqlException) {
            throw AccessUtils.translate("oracle ps batch", sql, sqlException, runtimeContext.getConfig().getDataSource());
        }
        return psRes;
    }
}
