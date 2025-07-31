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
package easy4j.infra.dbaccess.dynamic.schema;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.DBAccess;

import java.util.List;

/**
 * OracleDyInformationSchema
 *
 * @author bokun.li
 * @date 2025-07-31 20:11:58
 */
public class OracleDyInformationSchema extends AbstractDyInformationSchema {


    public static final String sql = "SELECT \n" +
            "  col.column_name AS column_name,\n" +
            "  col.data_type AS data_type,\n" +
            "  col.data_length AS str_max_len,\n" +
            "  col.data_precision AS number_precision,\n" +
            "  CASE WHEN col.nullable = 'Y' THEN 'YES' ELSE 'NO' END AS is_nullable,\n" +
            "  CASE WHEN con.column_name IS NOT NULL THEN 'PRI' ELSE '' END AS is_pre\n" +
            "FROM \n" +
            "  user_tab_columns col\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    cc.column_name\n" +
            "  FROM \n" +
            "    user_constraints c\n" +
            "  JOIN \n" +
            "    user_cons_columns cc ON c.constraint_name = cc.constraint_name\n" +
            "  WHERE \n" +
            "    c.table_name = ?\n" +
            "    AND c.constraint_type = 'P'\n" +
            ") con ON col.column_name = con.column_name\n" +
            "WHERE \n" +
            "  col.table_name = ?";

    DBAccess dbAccess;

    @Override
    public void setDbAccess(DBAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    @Override
    public DBAccess getDbAccess() {
        return this.dbAccess;
    }


    @Override
    public List<DynamicColumn> getColumns(String schema, String table) {
        if (StrUtil.isBlank(table)) {
            return ListTs.newArrayList();
        }
        table = table.toUpperCase();
        return dbAccess.selectList(sql, DynamicColumn.class, table, table);
    }
}