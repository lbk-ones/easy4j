package easy4j.infra.dbaccess.orm.sql;

import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlFactory {
    private static List<ISql> sqlList = new ArrayList<>();

    static {
        sqlList.add(new InsertSql());
        sqlList.add(new DeleteSql());
        sqlList.add(new QuerySql());
        sqlList.add(new UpdateSql());
        sqlList.add(new CountSql());
        sqlList.add(new ExistsSql());
        sqlList.add(new QueryPageSql());
        sqlList = Collections.unmodifiableList(sqlList);
    }

    // 写入sql
    public static <T> void parse(RuntimeContext<T> runtimeContext) {
        boolean look = false;
        for (ISql iSql : sqlList) {
            if (iSql.match(runtimeContext)) {
                String build = iSql.build(runtimeContext);
                runtimeContext.setSql(build);
                look = true;
                break;
            }
        }
        if (!look) runtimeContext.setSql("");
    }


}
