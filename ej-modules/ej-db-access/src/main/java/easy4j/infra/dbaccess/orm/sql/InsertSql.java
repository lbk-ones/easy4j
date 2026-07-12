package easy4j.infra.dbaccess.orm.sql;


import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

public class InsertSql implements ISql{

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.INSERT;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        return "";
    }
}
