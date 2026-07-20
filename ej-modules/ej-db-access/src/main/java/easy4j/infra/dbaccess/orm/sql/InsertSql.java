package easy4j.infra.dbaccess.orm.sql;


import cn.hutool.core.util.StrUtil;
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

        String sql = "insert into " + dotTableName;
        List<AccessField> insertFields1 = runtimeContext.getInsertFields();
        List<AccessField> insertFields = runtimeContext.getColumnInfoList(insertFields1);
        Set<String> fields = new HashSet<>();
        for (AccessField insertField : insertFields) {
            String escapeColumnName = insertField.getEscapeColumnName();
            fields.add(escapeColumnName);
        }
        if (!fields.isEmpty()) {
            sql += "(" + ListTs.join(SP.SPACE + SP.COMMA + SP.SPACE, fields) + ")";
        }
        sql += " values ";
        Map<String, List<AccessField>> integerListMap = ListTs.groupBy(insertFields1, e->String.valueOf(e.getGroup()));
        List<String> valueList = new ArrayList<>();

        for (Map.Entry<String, List<AccessField>> integerListEntry : integerListMap.entrySet()) {
            List<AccessField> value = integerListEntry.getValue();
            String te = "(";
            te += value.stream().map(e -> SP.QUESTION_MARK).collect(Collectors.joining(SP.COMMA));
            te += ")";
            valueList.add(te);
        }
        String join = ListTs.join(SP.COMMA, valueList);
        sql += join;
        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            sql += SP.SPACE + lastSql;
        }
        return sql;
    }
}
