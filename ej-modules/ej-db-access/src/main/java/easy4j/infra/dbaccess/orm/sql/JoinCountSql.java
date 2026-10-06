package easy4j.infra.dbaccess.orm.sql;

import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;


public class JoinCountSql extends JoinSql {

    public JoinCountSql() {
        super.setCount(true);
    }

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT_JOIN_COUNT;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        return super.build(runtimeContext);
    }
}
