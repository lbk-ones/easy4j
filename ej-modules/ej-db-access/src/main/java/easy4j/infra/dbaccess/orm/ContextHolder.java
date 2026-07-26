package easy4j.infra.dbaccess.orm;

import javax.sql.DataSource;

public class ContextHolder {

    private final static ThreadLocal<DataSource> HOLDER = new InheritableThreadLocal<>();

    public static void set(DataSource dataSource) {
        if (dataSource != null) HOLDER.set(dataSource);
    }

    public static DataSource getDataSource(){
        return HOLDER.get();
    }

    public static void remove() {
        HOLDER.remove();
    }
}
