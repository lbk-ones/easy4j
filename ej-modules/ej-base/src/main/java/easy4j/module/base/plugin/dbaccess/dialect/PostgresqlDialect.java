package easy4j.module.base.plugin.dbaccess.dialect;


import easy4j.module.base.plugin.dbaccess.Page;

/**
 * Postgresql数据库方言实现
 */
public class PostgresqlDialect extends AbstractDialect {
    /**
     * Postgresql分页通过limit实现
     */
    public String getPageSql(String sql, Page<?> page) {
        return getPageBefore(sql, page) +
                sql +
                getPageAfter(sql, page);
    }


    public String getPageBefore(String sql, Page<?> page) {
        return "";
    }

    public String getPageAfter(String sql, Page<?> page) {
        int start = (page.getPageNo() - 1) * page.getPageSize();
        return " limit " + page.getPageSize() + " offset " + start;
    }
}
