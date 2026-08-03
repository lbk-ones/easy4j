package easy4j.infra.dbaccess.dialect.impl;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.AbstractDialect;
import easy4j.infra.dbaccess.dll.DB2FieldType;
import easy4j.infra.dbaccess.dll.OracleFieldType;

import java.sql.Connection;
import java.util.Optional;

/**
 * DB2Dialect
 * @author bokun.li
 * @date 2025/10/13
 */
public class DB2Dialect extends AbstractDialect {


    public DB2Dialect(Connection connection) {
        super(connection);
    }

    @Override
    public String getPageSql(String sql, Page<?> page) {
        int majorVersion = getMajorVersion();
        if (majorVersion >= 10) {
            // 现代分页方式：使用 OFFSET / FETCH FIRST
            StringBuilder pageSql = new StringBuilder(sql.length() + 100);
            pageSql.append(sql);

            int offset = (page.getPageNo() - 1) * page.getPageSize();
            int fetchSize = page.getPageSize();

            pageSql.append(" OFFSET ");
            pageSql.append(offset);
            pageSql.append(" ROWS FETCH FIRST ");
            pageSql.append(fetchSize);
            pageSql.append(" ROWS ONLY");

            return pageSql.toString();
        } else {
            // 传统分页方式：ROWNUMBER() OVER()
            StringBuilder pageSql = new StringBuilder(sql.length() + 100);
            pageSql.append("SELECT * FROM ( SELECT B.*, ROWNUMBER() OVER() AS RN FROM ( ");
            pageSql.append(sql);

            int start = (page.getPageNo() - 1) * page.getPageSize() + 1;
            int end = page.getPageNo() * page.getPageSize();

            pageSql.append(" ) AS B ) AS A WHERE A.RN BETWEEN ");
            pageSql.append(start);
            pageSql.append(" AND ");
            pageSql.append(end);

            return pageSql.toString();
        }
    }

    @Override
    public String strConvertToDate(String str) {
        if (StrUtil.isNotBlank(str)) {
            return "TIMESTAMP_FORMAT('" + str + "', 'YYYY-MM-DD HH:MI:SS')";
        } else {
            return str;
        }
    }

    @Override
    public boolean isLob(String typeName) {
        DB2FieldType fromDataType1 = DB2FieldType.getFromDataType(typeName);
        return fromDataType1 == DB2FieldType.CLOB;
    }

    @Override
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName) {
        typeName = ListTs.get(StrUtil.split(typeName, "#"), 0);
        return Optional.ofNullable(OracleFieldType.getFromDataType(typeName)).map(OracleFieldType::getJavaTypes).map(e -> e.length > 0 ? e[0] : null).orElse(null);
    }

    @Override
    public boolean isJson(String typeName) {
        return false;
    }

    @Override
    public String getDefaultDateTime() {
        return "current_timestamp";
    }
}
