package easy4j.infra.dbaccess.dialect.v2.impl;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.AbstractDialectV2;
import easy4j.infra.dbaccess.dynamic.dll.OracleFieldType;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
/**
 * OracleDialect
 * @author bokun.li
 * @date 2025/10/13
 */
public class OracleDialect extends AbstractDialectV2 {


    public OracleDialect(Connection connection) {
        super(connection);
    }

    @Override
    public boolean isLob(String typeName) {
        OracleFieldType fromDataType1 = OracleFieldType.getFromDataType(typeName);
        return fromDataType1 == OracleFieldType.CLOB;
    }

    @Override
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName) {
        return Optional.ofNullable(OracleFieldType.getFromDataType(typeName)).map(OracleFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);}

    @Override
    public boolean isJson(String typeName) {
        return false;
    }

    @Override
    public String getPageSql(String sql, Page<?> page) {
        StringBuilder pageSql = new StringBuilder(sql.length() + 100);
        pageSql.append("select * from ( select row_.*, rownum rownum_ from ( ");
        pageSql.append(sql);
        int start = (page.getPageNo() - 1) * page.getPageSize() + 1;
        pageSql.append(" ) row_ where rownum < ");
        pageSql.append(start + page.getPageSize());
        pageSql.append(" ) where rownum_ >= ");
        pageSql.append(start);
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
