package easy4j.infra.dbaccess.dialect.v2.impl;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.AbstractDialectV2;

import java.sql.Connection;
/**
 * DB2Dialect
 * @author bokun.li
 * @date 2025/10/13
 */
public class DB2Dialect extends AbstractDialectV2 {


    public DB2Dialect(Connection connection) {
        super(connection);
    }

    @Override
    public String getPageSql(String sql, Page<?> page) {
        StringBuilder pageSql = new StringBuilder(sql.length() + 100);
        pageSql.append("SELECT * FROM  ( SELECT B.*, ROWNUMBER() OVER() AS RN FROM ( ");
        pageSql.append(sql);
        int start = (page.getPageNo() - 1) * page.getPageSize() + 1;
        pageSql.append(" ) AS B )AS A WHERE A.RN BETWEEN ");
        pageSql.append(start);
        pageSql.append(" AND ");
        pageSql.append(start + page.getPageSize());
        return pageSql.toString();
    }

    @Override
    public String strConvertToDate(String str) {
        if (StrUtil.isNotBlank(str)) {
            return "TO_TIMESTAMP('" + str + "', 'YYYY-MM-DD HH24:MI:SS')";
        } else {
            return str;
        }
    }

    @Override
    public boolean isLob(String typeName) {
        return false;
    }

    @Override
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName) {
        return null;
    }

    @Override
    public boolean isJson(String typeName) {
        return false;
    }

    @Override
    public String getDefaultDateTime() {
        return null;
    }
}
