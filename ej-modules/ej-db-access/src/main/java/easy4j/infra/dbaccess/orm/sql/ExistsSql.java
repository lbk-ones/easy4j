package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

public class ExistsSql implements ISql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT_EXIST;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String whereSql = runtimeContext.getWhereSql();

        String sql = "select count(1) from " +
                runtimeContext.getDotTableName() +
                SP.SPACE +
                "where" +
                SP.SPACE +
                whereSql;
        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            sql += SP.SPACE + lastSql;
        }
        return sql;
    }
}
