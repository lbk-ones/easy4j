package easy4j.module.base.plugin.dbaccess.dialect;


import easy4j.module.base.plugin.dbaccess.Page;

/**
 * db2方言
 */
public class Db2Dialect extends AbstractDialect {
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
}
