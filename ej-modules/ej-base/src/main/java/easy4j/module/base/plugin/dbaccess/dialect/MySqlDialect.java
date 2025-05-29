package easy4j.module.base.plugin.dbaccess.dialect;


import cn.hutool.db.sql.Wrapper;
import easy4j.module.base.plugin.dbaccess.Page;

/**
 * Mysql数据库方言实现
 */
public class MySqlDialect extends AbstractDialect {
    /**
     * mysql分页通过limit实现
     */
    public String getPageSql(String sql, Page<?> page) {
        StringBuilder pageSql = new StringBuilder(sql.length() + 100);
        pageSql.append(sql);
        int start = (page.getPageNo() - 1) * page.getPageSize();
        pageSql.append(" limit ").append(start).append(",").append(page.getPageSize());
        return pageSql.toString();
    }

    @Override
    public Wrapper getWrapper() {
        return new Wrapper('`');
    }
}
