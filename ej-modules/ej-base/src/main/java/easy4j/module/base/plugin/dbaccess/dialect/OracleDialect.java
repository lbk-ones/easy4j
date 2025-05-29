/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.base.plugin.dbaccess.dialect;


import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.Page;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.plugin.dbaccess.helper.OracleTypeConverter;
import easy4j.module.base.utils.ListTs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Oracle数据库方言实现
 */
public class OracleDialect extends AbstractDialect {
    /**
     * oracle分页通过rownum实现
     */
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

    /**
     * oracle 批量写入有所不同
     * INSERT INTO employee (id, name)
     * SELECT id, name FROM (
     * SELECT 1 AS id, '张三' AS name FROM DUAL
     * UNION ALL
     * SELECT 2 AS id, '李四' AS name FROM DUAL
     * )
     * 全部转为大写 oracle是偏大写的
     *
     * @param tableName
     * @param columns
     * @param recordList
     * @param connection
     * @return
     * @throws SQLException
     */
    @Override
    public PreparedStatement psForBatchInsert(String tableName, String[] columns, List<Map<String, Object>> recordList, Connection connection) {
        if (recordList.isEmpty()) {
            throw new EasyException("recordList is empty!");
        }
        // 单条不走批量
        if (recordList.size() == 1) {
            return psForInsert(tableName, columns, recordList.get(0), connection);
        }
        Dialect dialect = JdbcHelper.getDialect(connection);
        assert dialect != null;
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO");
        stringBuilder.append(" ");
        stringBuilder.append(tableName);
        stringBuilder.append("(");
        List<String> columnsList = ListTs.newArrayList();
        Wrapper wrapper = getWrapper();
        for (String column : columns) {
            String wrap = StrUtil.blankToDefault(wrapper.wrap(StrUtil.toUnderlineCase(column)), "");
            columnsList.add(wrap.toUpperCase());
        }
        stringBuilder.append(String.join(",", columnsList));
        stringBuilder.append(")");
        stringBuilder.append(" ");
        stringBuilder.append("SELECT ");
        stringBuilder.append(String.join(",", columnsList));
        stringBuilder.append(" FROM ");
        stringBuilder.append("(");
        StringBuilder insSql;
        Map<String, Object> stringObjectMap = null;
        for (int i = 0; i < recordList.size(); i++) {
            stringObjectMap = recordList.get(i);
            insSql = new StringBuilder("SELECT");
            insSql.append(" ");
            // columns 没下划线
            for (String column : columns) {
                Object o = stringObjectMap.get(column);
                Class<?> aClass1 = o.getClass();
                String s = OracleTypeConverter.convertToOracleExpression(aClass1, o);
                // 转成下划线
                insSql.append(s).append(" AS ").append(wrapper.wrap(StrUtil.toUnderlineCase(column)));
            }
            insSql.append(" ");
            insSql.append("FROM DUAL");
            if (i != recordList.size() - 1) {
                insSql.append(" UNION ALL ");
            }
            stringBuilder.append(insSql);
        }
        stringBuilder.append(")");
        String string = stringBuilder.toString();
        try {
            return connection.prepareStatement(string);
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("psForBatchInsert", string, e);
        }
    }
}
