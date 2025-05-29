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
import easy4j.module.base.plugin.dbaccess.Page;

/**
 * SQLServer数据库方言实现
 */
public class SQLServerDialect extends AbstractDialect {
    private static final String STR_ORDERBY = " order by ";

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
    public Wrapper getWrapper() {
        return new Wrapper('[', ']');
    }
}
