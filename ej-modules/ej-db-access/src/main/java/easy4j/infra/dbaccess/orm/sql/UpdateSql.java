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
        String whereSql = runtimeContext.getWhereSql();
        List<AccessField> updateFields = runtimeContext.getColumnInfoList(runtimeContext.getUpdateFields());
        String lastSql = runtimeContext.getLastSql();
        String s;
        if (!updateFields.isEmpty()) {
            s = "update " +
                    runtimeContext.getDotTableName() +
                    SP.SPACE +
                    "set" +
                    SP.SPACE +
                    ListTs.join(SP.COMMA, updateFields.stream().map(e -> e.getEscapeColumnName() + " = ? ").toList());
        } else {
            s = "update " + runtimeContext.getDotTableName() + SP.SPACE + "set" + SP.SPACE + ListTs.join(SP.COMMA, runtimeContext.getSqlSet());
        }

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
