package easy4j.infra.dbaccess.orm.sql;


import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessField;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.util.*;
import java.util.stream.Collectors;

public class InsertSql implements ISql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.INSERT;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
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
        // sql server 的自增需要单独处理
        if (Objects.equals(runtimeContext.getDbType(), DbType.SQL_SERVER.getDb())) {
            sql.append(SP.SPACE);
            List<AccessField> list = runtimeContext.getColumnInfoList().stream().filter(AccessField::isAutoIncrementIs).toList();
            if (!list.isEmpty()) {
                sql.append("output");
                int i = 0;
                for (AccessField accessField : list) {
                    if (i == 0) {
                        sql.append(" ");
                    } else {
                        sql.append(",");
                    }
                    sql.append("inserted.").append(accessField.getEscapeColumnName());
                    i++;
                }
            }
        }
        sql.append(SP.SPACE);
        sql.append("values");
        sql.append(SP.SPACE);

        Map<String, List<AccessField>> integerListMap = ListTs.groupBy(insertFieldsList, e -> String.valueOf(e.getGroup()));
        TreeMap<String, List<AccessField>> treeMap = new TreeMap<>(integerListMap);
        List<String> valueList = new ArrayList<>();
        for (Map.Entry<String, List<AccessField>> integerListEntry : treeMap.entrySet()) {
            List<AccessField> value = integerListEntry.getValue();
            String te = "(";
            te += value.stream().map(AccessField::getPlaceHolder).collect(Collectors.joining(SP.COMMA));
            te += ")";
            valueList.add(te);
        }
        String join = ListTs.join(SP.COMMA, valueList);
        sql.append(join);
        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            sql.append(SP.SPACE).append(lastSql);
        }
        return sql.toString();
    }
}
