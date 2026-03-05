package easy4j.infra.dbaccess.dialect.v2.impl;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.AbstractDialectV2;
import easy4j.infra.dbaccess.dynamic.dll.SqlServerFieldType;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
/**
 * SQLServerDialect
 * @author bokun.li
 * @date 2025/10/13
 */
public class SQLServerDialect extends AbstractDialectV2 {
    private static final String STR_ORDERBY = " order by ";
    public SQLServerDialect(Connection connection) {
        super(connection);
        super.setPrintLog(true);
    }


    @Override
    public boolean isLob(String typeName) {
        SqlServerFieldType fromDataType1 = SqlServerFieldType.getFromDataType(typeName);
        return fromDataType1 == SqlServerFieldType.VARCHAR_MAX || fromDataType1 == SqlServerFieldType.NVARCHAR_MAX || fromDataType1 == SqlServerFieldType.TEXT || fromDataType1 == SqlServerFieldType.NTEXT;
    }

    @Override
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName) {
        return Optional.ofNullable(SqlServerFieldType.getFromDataType(typeName)).map(SqlServerFieldType::getJavaTypes).map(e -> e.length > 0 ? e[0] : null).orElse(null);
    }

    @Override
    public boolean isJson(String typeName) {
        return false;
    }

    @Override
    public String getPageSql(String sql, Page<?> page) {
        int orderIdx = sql.indexOf(STR_ORDERBY);
        String orderStr = null;
        if (orderIdx != -1) {
            orderStr = sql.substring(orderIdx + 10);
            sql = sql.substring(0, orderIdx);
        }
        StringBuilder pageSql = new StringBuilder();
        pageSql.append("select top ");
        pageSql.append(page.getPageSize());
        pageSql.append(" * from (select row_number() over (");
        String orderBy = getOrderBy(sql, orderStr);
        pageSql.append(orderBy);
        pageSql.append(") row_number, * from (");
        pageSql.append(sql);
        int start = (page.getPageNo() - 1) * page.getPageSize();
        pageSql.append(") aa ) a where row_number > ");
        pageSql.append(start);
        pageSql.append(" order by row_number");
        return pageSql.toString();
    }

    public String getOrderBy(String sql, String orderBy) {
        if (StrUtil.isEmpty(orderBy)) {
            return STR_ORDERBY + " id desc ";
        }
        StringBuilder orderBuffer = new StringBuilder(30);
        String[] orderByArray = StrUtil.split(orderBy, ',').toArray(new String[]{});
        for (String s : orderByArray) {
            String orderByItem = s.trim();
            String orderByName = null;
            String orderByDirect = "";
            if (!orderByItem.contains(" ")) {
                orderByName = orderByItem;
            } else {
                orderByName = orderByItem.substring(0, orderByItem.indexOf(" "));
                orderByDirect = orderByItem.substring(orderByItem.indexOf(" ") + 1);
            }
            if (orderByName.contains(".")) {
                orderByName = orderByName.substring(orderByName.indexOf(".") + 1);
            }
            String columnAlias = orderByName + " as ";
            int columnIndex = sql.indexOf(columnAlias);
            if (columnIndex == -1) {
                orderBuffer.append(orderByName).append(" ").append(orderByDirect).append(" ,");
            } else {
                String after = sql.substring(columnIndex + columnAlias.length());
                String aliasName = null;
                if (after.contains(",") && after.indexOf(" from") > after.indexOf(",")) {
                    aliasName = after.substring(0, after.indexOf(","));
                } else {
                    aliasName = after.substring(0, after.indexOf(" "));
                }
                orderBuffer.append(aliasName).append(" ").append(orderByDirect).append(" ,");
            }
        }
        orderBuffer.deleteCharAt(orderBuffer.length() - 1);
        return STR_ORDERBY + orderBuffer;
    }

    @Override
    public String strConvertToDate(String str) {
        if (StrUtil.isNotBlank(str)) {
            return "CONVERT(datetime, '" + str + "')";
        } else {
            return str;
        }
    }

    @Override
    public String getDefaultDateTime() {
        return "SYSDATETIME()";
    }
}
