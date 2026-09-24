package easy4j.infra.dbaccess.orm.sql;

import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

// select * from table where xxx
public class JoinPageSql extends JoinSql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT_JOIN_PAGE;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String build = super.build(runtimeContext);
        return runtimeContext.getDialect().getPageSql(build.trim(), runtimeContext.getPage());
    }
}
