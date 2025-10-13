package easy4j.infra.dbaccess.dialect.v2.impl;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.AbstractDialectV2;
import easy4j.infra.dbaccess.dynamic.dll.MySQLFieldType;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
/**
 * MysqlDialect
 * @author bokun.li
 * @date 2025/10/13
 */
public class MysqlDialect extends AbstractDialectV2 {


    public MysqlDialect(Connection connection) {
        super(connection);
    }


    @Override
    public boolean isLob(String typeName) {
        return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.LONGTEXT;
    }

    @Override
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName) {
        return Optional.ofNullable(MySQLFieldType.getFromDataType(typeName)).map(MySQLFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
    }

    @Override
    public boolean isJson(String typeName) {
        return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.JSON;
    }

    @Override
    public String getPageSql(String sql, Page<?> page) {
        StringBuilder pageSql = new StringBuilder(sql.length() + 100);
        pageSql.append(sql);
        int start = (page.getPageNo() - 1) * page.getPageSize();
        pageSql.append(" limit ").append(start).append(",").append(page.getPageSize());
        return pageSql.toString();
    }

    @Override
    public String strConvertToDate(String str) {
        if (StrUtil.isNotBlank(str)) {
            return "CAST('" + str + "' AS DATETIME)";
        } else {
            return str;
        }
    }

    @Override
    public String getDefaultDateTime() {
        return "current_date";
    }
}
