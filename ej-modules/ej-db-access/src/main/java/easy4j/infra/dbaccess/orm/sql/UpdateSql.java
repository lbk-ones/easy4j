package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessField;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.util.List;

public class UpdateSql implements ISql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.UPDATE;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String schema = runtimeContext.getSchema();
        String tableName = runtimeContext.getTableName();
        String whereSql = runtimeContext.getWhereSql();
        List<AccessField> updateFields = runtimeContext.getUpdateFields();
        String lastSql = runtimeContext.getLastSql();
        String s = "update " +
                ListTs.join(SP.DOT, ListTs.asList(schema, tableName)) +
                SP.SPACE +
                "set" +
                SP.SPACE +
                ListTs.join(SP.COMMA, updateFields.stream().map(e -> e.getColumnName() + " = ? ").toList());
        if (StrUtil.isNotBlank(whereSql)) {
            s += SP.SPACE +
                    "where" +
                    SP.SPACE +
                    whereSql;
        }
        if (StrUtil.isNotBlank(lastSql)) {
            s += SP.SPACE + lastSql;
        }
        return s;
    }
}
