package easy4j.infra.dbaccess.orm.sql;

import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.sql.dialect.ISqlDialect;
import easy4j.infra.dbaccess.orm.sql.dialect.SqlDialectFactory;

import java.util.Objects;

public abstract class AbsISql implements ISql {

    @Override
    public <T> String exe(RuntimeContext<T> runtimeContext) {
        String s = exeDialect(runtimeContext);
        if (Objects.equals(s, SP.DASH)) {
            return build(runtimeContext);
        } else {
            return s;
        }
    }

    public String exeDialect(RuntimeContext<?> context) {
        ISqlDialect iSqlDialect = SqlDialectFactory.get(context);
        if (iSqlDialect != null) {
            context.setPsOperateFunction(iSqlDialect);
            return iSqlDialect.build(context);
        } else {
            return SP.DASH;
        }
    }

}
