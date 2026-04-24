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
        super.setPrintLog(true);
    }


    @Override
    public boolean isLob(String typeName) {
        return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.LONGTEXT;
    }

    @Override
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName) {
        List<String> split = StrUtil.split(typeName, "#");
        String _typeName = ListTs.get(split, 0);
        String columnSize = ListTs.get(split, 1);
        // 这里兼容下 TINYINT(1) 这种情况 这种直接转为 int
        Class<?> aClass = Optional.ofNullable(MySQLFieldType.getFromDataType(_typeName)).map(MySQLFieldType::getJavaTypes).map(e -> e.length > 0 ? e[0] : null).orElse(null);
        if(StrUtil.isNotBlank(columnSize) && "1".equals(columnSize)){
            if("bit".equalsIgnoreCase(_typeName) && byte[].class == aClass){
                aClass = int.class;
            }
        }
        return aClass;
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
