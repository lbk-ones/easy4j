package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessField;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.util.List;

// delete table where id in (x1,x2,x3)
public class DeleteSql implements ISql {
    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.DELETE;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String whereSql = runtimeContext.getWhereSql();

        String sql = "delete from " +
                runtimeContext.getDotTableName();
        sql = runtimeContext.getAccessUtils().appendWhere(sql,whereSql);
        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            sql += SP.SPACE + lastSql;
        }
        return sql;
    }
}
