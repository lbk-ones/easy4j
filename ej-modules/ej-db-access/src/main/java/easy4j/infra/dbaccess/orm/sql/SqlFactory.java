package easy4j.infra.dbaccess.orm.sql;

import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.sql.dialect.ISqlDialect;
import easy4j.infra.dbaccess.orm.sql.dialect.SqlDialectFactory;

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
        sqlList.add(new JoinCountSql());
        sqlList.add(new JoinPageSql());
        sqlList.add(new ExistsSql());
        sqlList.add(new QueryPageSql());
        sqlList.add(new TruncateSql());
        sqlList.add(new JoinSql());
        sqlList = Collections.unmodifiableList(sqlList);
    }

    // 写入sql
    public static <T> void parse(RuntimeContext<T> runtimeContext) {
        boolean look = false;
        for (ISql iSql : sqlList) {
            if (iSql.match(runtimeContext)) {
                // 这里处理不同的OperateType在不同的方言下面的表现形式
                String build = iSql.exe(runtimeContext);
                runtimeContext.setSql(build);
                look = true;
                break;
            }
        }
        if (!look) {
            // 这里有些特殊的会直接以方言的形式出现，所以这里再找一次
            ISqlDialect iSqlDialect = SqlDialectFactory.get(runtimeContext);
            if (iSqlDialect != null) {
                runtimeContext.setPsOperateFunction(iSqlDialect);
                String build = iSqlDialect.build(runtimeContext);
                runtimeContext.setSql(build);
            } else {
                runtimeContext.setSql("");
            }
        }
    }


}
