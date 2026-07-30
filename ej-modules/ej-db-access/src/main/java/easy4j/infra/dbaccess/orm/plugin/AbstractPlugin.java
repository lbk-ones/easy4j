package easy4j.infra.dbaccess.orm.plugin;

import easy4j.infra.dbaccess.orm.Access;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import javax.sql.DataSource;

public abstract class AbstractPlugin implements IPlugin {

    @Override
    public abstract String getName();

    @Override
    public DataSource getDataSource(Access<?> access) {
        return null;
    }

    @Override
    public void contextPrepared(RuntimeContext<?> context) {

    }

    @Override
    public void init(Access<?> access) {

    }

    @Override
    public void beforeReturn(RuntimeContext<?> context) {

    }

    @Override
    public void finish(RuntimeContext<?> context) {

    }
}
