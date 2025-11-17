package easy4j.infra.dbaccess.dialect.v2.impl;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.AbstractDialectV2;
import easy4j.infra.dbaccess.dynamic.dll.PgSQLFieldType;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
/**
 * PostgresqlDialect
 * @author bokun.li
 * @date 2025/10/13
 */
public class PostgresqlDialect extends AbstractDialectV2 {


    public PostgresqlDialect(Connection connection) {
        super(connection);
        super.setPrintLog(true);
    }

    @Override
    public boolean isLob(String typeName) {
        PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(typeName);
        return fromDataType == PgSQLFieldType.TEXT;
    }

    @Override
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName) {
        return Optional.ofNullable(PgSQLFieldType.getFromDataType(typeName)).map(PgSQLFieldType::getJavaTypes).map(e -> e.length > 0 ? e[0] : null).orElse(null);}

    @Override
    public boolean isJson(String typeName) {
        PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(typeName);
        return fromDataType == PgSQLFieldType.JSON || fromDataType == PgSQLFieldType.JSONB;
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
            return "TO_TIMESTAMP('" + str + "', 'YYYY-MM-DD HH24:MI:SS')";
        } else {
            return str;
        }
    }

    @Override
    public String getDefaultDateTime() {
        return "current_timestamp";
    }
}
