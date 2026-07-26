package easy4j.infra.dbaccess.orm.sql.dialect;

import easy4j.infra.common.utils.ServiceLoaderUtils;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.util.List;

/**
 * 获取sql方言接口
 */
public class SqlDialectFactory {

    private final static List<ISqlDialect> iSqlDialectList = ServiceLoaderUtils.load(ISqlDialect.class);


    public static ISqlDialect get(RuntimeContext<?> context){
        for (ISqlDialect iSqlDialect : iSqlDialectList) {
            boolean match = iSqlDialect.match(context);
            if(match){
                return iSqlDialect;
            }
        }
        return null;
    }


}
