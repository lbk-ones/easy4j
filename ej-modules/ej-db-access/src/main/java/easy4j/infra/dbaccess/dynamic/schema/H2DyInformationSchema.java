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

/**
 * H2DyInformationSchema
 *
 * @author bokun.li
 * @date 2025-07-31 20:11:58
 */
public class H2DyInformationSchema extends AbstractDyInformationSchema {


    public static final String sql = "SELECT \n" +
            "  c.COLUMN_NAME AS column_name,\n" +
            "  c.DATA_TYPE AS data_type,\n" +
            "  CASE WHEN c.DATA_TYPE IN ('VARCHAR', 'CHAR', 'NVARCHAR', 'NCHAR') \n" +
            "       THEN c.CHARACTER_MAXIMUM_LENGTH \n" +
            "       ELSE NULL \n" +
            "  END AS str_max_len,\n" +
            "  CASE WHEN c.DATA_TYPE IN ('INT', 'BIGINT', 'DECIMAL', 'NUMERIC', 'FLOAT', 'DOUBLE') \n" +
            "       THEN c.NUMERIC_PRECISION \n" +
            "       ELSE NULL \n" +
            "  END AS number_precision,\n" +
            "  c.IS_NULLABLE AS is_nullable,\n" +
            "  CASE \n" +
            "    WHEN kcu.COLUMN_NAME IS NOT NULL THEN 'PRI' \n" +
            "    ELSE '' \n" +
            "  END AS is_primary \n" +
            "FROM \n" +
            "  INFORMATION_SCHEMA.COLUMNS c\n" +
            "LEFT JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu \n" +
            "  ON c.TABLE_NAME = kcu.TABLE_NAME \n" +
            "  AND c.COLUMN_NAME = kcu.COLUMN_NAME \n" +
            "  AND c.TABLE_SCHEMA = kcu.TABLE_SCHEMA\n" +
            "LEFT JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc \n" +
            "  ON kcu.CONSTRAINT_NAME = tc.CONSTRAINT_NAME \n" +
            "  AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY'\n" +
            "WHERE \n" +
            "  c.TABLE_SCHEMA = ?\n" +
            "  AND c.TABLE_NAME = ?";

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
        if (StrUtil.isBlank(schema)) {
            schema = "PUBLIC";
        }

        return dbAccess.selectList(sql, DynamicColumn.class, schema, table);
    }
}