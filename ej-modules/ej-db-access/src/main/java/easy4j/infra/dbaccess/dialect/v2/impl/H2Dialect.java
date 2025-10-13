package easy4j.infra.dbaccess.dialect.v2.impl;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.AbstractDialectV2;
import easy4j.infra.dbaccess.dynamic.dll.H2SqlFieldType;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
/**
 * H2Dialect
 * @author bokun.li
 * @date 2025/10/13
 */
public class H2Dialect extends AbstractDialectV2 {

    public H2Dialect(Connection connection) {
        super(connection);
    }

    @Override
    public boolean isLob(String typeName) {
        H2SqlFieldType fromDataType1 = H2SqlFieldType.getFromDataType(typeName);
        return fromDataType1 == H2SqlFieldType.CLOB || fromDataType1 == H2SqlFieldType.TEXT;
    }

    @Override
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName) {
        return Optional.ofNullable(H2SqlFieldType.getFromDataType(typeName)).map(H2SqlFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
    }

    @Override
    public boolean isJson(String typeName) {
        H2SqlFieldType fromDataType = H2SqlFieldType.getFromDataType(typeName);
        return fromDataType == H2SqlFieldType.JSON;
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
            return "CAST('" + str + "' as TIMESTAMP)";
        } else {
            return str;
        }
    }


    @Override
    public String getDefaultDateTime() {
        return "current_timestamp";
    }
}
