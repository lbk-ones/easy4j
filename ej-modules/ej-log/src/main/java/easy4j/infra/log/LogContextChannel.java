package easy4j.infra.log;

import easy4j.infra.context.ContextChannel;
import easy4j.infra.context.Easy4jContext;

public class LogContextChannel implements ContextChannel {
    public static Easy4jContext easy4jContext2;

    @Override
    public <T> T listener(String name, Class<T> aclass) {

        if (aclass == null) {
            return null;
        }
        if (DbLog.class == aclass || getDefaultName(DbLog.class).equals(name)) {
            DbLog dbLog = DbLog.getDbLog();
            return aclass.cast(dbLog);
        }
        return null;
    }

    @Override
    public void init(Easy4jContext easy4jContext) {
        easy4jContext2 = easy4jContext;
    }
}
