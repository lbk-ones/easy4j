package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.sql.dialect.ISqlDialect;
import easy4j.infra.dbaccess.orm.sql.dialect.SqlDialectFactory;

import java.util.List;
import java.util.Objects;

// select * from table where xxx
public class QueryPageSql  extends QuerySql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT_PAGE;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String build = super.build(runtimeContext);
        return runtimeContext.getDialect().getPageSql(build.trim(), runtimeContext.getPage());
    }
}
