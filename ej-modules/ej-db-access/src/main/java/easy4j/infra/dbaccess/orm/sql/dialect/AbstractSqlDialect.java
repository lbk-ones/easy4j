package easy4j.infra.dbaccess.orm.sql.dialect;

import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.runner.PsRes;

public abstract class AbstractSqlDialect implements ISqlDialect {

    @Override
    public PsRes prepareStatementAndExe(RuntimeContext<?> runtimeContext) {
        return null;
    }
}
