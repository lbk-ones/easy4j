package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.sql.dialect.ISqlDialect;
import easy4j.infra.dbaccess.orm.sql.dialect.SqlDialectFactory;

import java.util.Objects;

public class CountSql extends AbsISql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT_COUNT;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String whereSql = runtimeContext.getWhereSql();

        String sql = "select count(1) from " +
                runtimeContext.getDotTableName();

        sql = runtimeContext.getAccessUtils().appendWhere(sql, whereSql);

        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            sql += SP.SPACE + lastSql;
        }
        return sql;
    }
}
