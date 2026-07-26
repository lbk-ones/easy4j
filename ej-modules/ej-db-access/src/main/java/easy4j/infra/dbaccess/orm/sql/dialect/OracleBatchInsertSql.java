package easy4j.infra.dbaccess.orm.sql.dialect;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessField;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.runner.LogResult;
import easy4j.infra.dbaccess.orm.runner.PsRes;
import easy4j.infra.dbaccess.orm.runner.StatementUtils;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 另外一种批量写入的方式，但是这种写法不会带回写
 */
public class OracleBatchInsertSql extends AbstractSqlDialect {

    @Override
    public boolean match(RuntimeContext<?> context) {
        int oracleWriteStrategy = context.getAccessUtils().getAccessConfig().getOracleWriteStrategy();
        return context.getOperateType() == OperateType.INSERT && Objects.equals(context.getDbType(), DbType.ORACLE.getDb()) && oracleWriteStrategy != 1;
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

        /**
         select 1, '2' from dual
         union all
         select 2, '3' from dual
         */
        sql.append(SP.SPACE);
        sql.append(SP.LEFT_BRACKET);
        Map<String, List<AccessField>> integerListMap = ListTs.groupBy(insertFieldsList, e -> String.valueOf(e.getGroup()));
        TreeMap<String, List<AccessField>> treeMap = new TreeMap<>(integerListMap);
        List<String> subSqlList = new ArrayList<>();
        for (Map.Entry<String, List<AccessField>> integerListEntry : treeMap.entrySet()) {
            StringBuilder subSql = new StringBuilder("select");
            subSql.append(SP.SPACE);
            List<AccessField> value = integerListEntry.getValue();
            value.sort(Comparator.comparing(AccessField::getColumnName));
            String collect = value.stream().map(e -> {
                Object columnValue = e.getColumnValue();
                boolean isNull = columnValue == null;
                if (isNull) {
                    e.setSkipPsSet(true);
                }
                String t = isNull ? "null" : e.getPlaceHolder();

                return t + " as " + e.getEscapeColumnName();
            }).collect(Collectors.joining(SP.COMMA + SP.SPACE));

            subSql.append(collect);
            subSql.append(SP.SPACE);
            subSql.append("from dual");
            subSqlList.add(subSql.toString());
        }

        String join = ListTs.join(" union all ", subSqlList);
        sql.append(SP.SPACE);
        sql.append(join);
        sql.append(SP.SPACE);
        sql.append(SP.RIGHT_BRACKET);
        return sql.toString();
    }

    @Override
    public PsRes prepareStatementAndExe(RuntimeContext<?> runtimeContext) {
        String sql = runtimeContext.getSql();
        List<Object> args = runtimeContext.getArgs();
        PsRes psRes = new PsRes();
        Connection conn = runtimeContext.getConnection();
        try {
            PreparedStatement cstmt = conn.prepareStatement(sql);
            psRes.setStatement(cstmt);
            int effectRows = 0;
            StatementUtils.fillParams(runtimeContext, cstmt, args.toArray(new Object[]{}));
            int i = cstmt.executeUpdate();
            effectRows += i;
            runtimeContext.setTempEffectRows(i);
            psRes.setEffectRows(effectRows);
        } catch (SQLException sqlException) {
            throw AccessUtils.translate("oracle ps batch", sql, sqlException, runtimeContext.getConfig().getDataSource());
        }
        return psRes;
    }
}
