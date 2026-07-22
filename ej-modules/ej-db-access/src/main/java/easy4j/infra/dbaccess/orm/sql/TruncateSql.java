package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

/**
 * 截断
 */
public class TruncateSql implements  ISql{

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.TRUNCATE;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String sql  = "truncate table "+runtimeContext.getDotTableName();
        String dbType = runtimeContext.getDbType();
        if(StrUtil.equals(dbType, DbType.POSTGRE_SQL.getDb())){
            sql += SP.SPACE + "restart identity";
        }
        return sql;
    }
}
