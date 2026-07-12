package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.util.List;

// select * from table where xxx
public class QuerySql implements ISql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String sql = runtimeContext.getSql();
        if (StrUtil.isNotBlank(sql)) return sql;
        String tableName = runtimeContext.getTableName();
        String schema = runtimeContext.getSchema();
        String TEMP = "SELECT";
        List<String> selectFields = runtimeContext.getSelectFields();
        // 1
        if (CollUtil.isNotEmpty(selectFields)) {
            TEMP = TEMP + SP.SPACE + ListTs.join(SP.DOT, selectFields);
        } else {
            TEMP = TEMP + SP.SPACE + "*";
        }
        TEMP += " FROM ";

        // 2
        TEMP += ListTs.join(SP.DOT, ListTs.asList(schema, tableName));

        // 3
        String whereSql = runtimeContext.getWhereSql();
        if (!whereSql.isEmpty()) {
            TEMP = TEMP + " WHERE " + whereSql;
        }
        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            TEMP += SP.SPACE + lastSql;
        }
        return TEMP.trim();
    }
}
