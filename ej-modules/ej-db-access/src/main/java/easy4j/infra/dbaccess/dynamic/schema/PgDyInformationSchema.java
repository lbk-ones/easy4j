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
import easy4j.infra.dbaccess.DBAccess;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PgDyInformationSchema
 *
 * @author bokun.li
 * @date 2025-07-31 19:56:14
 */
public class PgDyInformationSchema extends AbstractDyInformationSchema {

    DBAccess jdbcDbAccess;


    String sql = "SELECT\n" +
            "  c.column_name AS column_name,\n" +
            "  c.data_type AS data_type,\n" +
            "  c.character_maximum_length AS str_max_len,\n" +
            "  c.numeric_precision AS number_precision,\n" +
            "  c.is_nullable AS is_nullable,\n" +
            "  CASE WHEN p.conname IS NOT NULL THEN 'PRI' ELSE '' END AS is_pre\n" +
            "FROM\n" +
            "  information_schema.columns c\n" +
            "LEFT JOIN (\n" +
            "  SELECT\n" +
            "    a.attname AS column_name,\n" +
            "    conname\n" +
            "  FROM\n" +
            "    pg_constraint p\n" +
            "  JOIN\n" +
            "    pg_class t ON p.conrelid = t.oid\n" +
            "  JOIN\n" +
            "    pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(p.conkey)\n" +
            "  WHERE\n" +
            "    t.relname = ?\n" +
            "    AND p.contype = 'p'\n" +
            ") p ON c.column_name = p.column_name\n" +
            "WHERE\n" +
            "  c.table_schema = ?\n" +
            "  AND c.table_name = ?";

    @Override
    public void setDbAccess(DBAccess dbAccess) {
        this.jdbcDbAccess = dbAccess;
    }


    @Override
    public DBAccess getDbAccess() {
        return this.jdbcDbAccess;
    }

    @Override
    public List<DynamicColumn> getColumns(String schema, String table) {
        if (StrUtil.isBlank(schema)) {
            schema = "public";
        }
        return jdbcDbAccess.selectList(sql, DynamicColumn.class, table, schema, table);
    }

    @Override
    public String getVersion() {
        String version = super.getVersion();
        return extractVersion(version);
    }
}