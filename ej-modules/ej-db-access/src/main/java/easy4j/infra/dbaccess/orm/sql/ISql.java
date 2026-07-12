package easy4j.infra.dbaccess.orm.sql;

import easy4j.infra.dbaccess.orm.RuntimeContext;

public interface ISql {

    <T> boolean match(RuntimeContext<T> runtimeContext);


    <T> String build(RuntimeContext<T> runtimeContext);

}
