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

import easy4j.infra.dbaccess.DBAccess;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MysqlDyInformationSchema
 *
 * @author bokun.li
 * @date 2025-07-31 20:11:58
 */
public class SqlServerDyInformationSchema extends AbstractDyInformationSchema {


    public static final String sql = "SELECT col.name                          AS column_name,\n" +
            "       typ.name                                                   AS data_type,\n" +
            "       col.max_length / 2                                         AS str_max_len,\n" +
            "       col.precision                                              AS number_precision,\n" +
            "       CASE WHEN col.is_nullable = 1 THEN 'YES' ELSE 'NO' END     AS is_nullable,\n" +
            "       CASE WHEN idx.column_id IS NOT NULL THEN 'PRI' ELSE '' END AS is_pre\n" +
            "FROM sys.columns col\n" +
            "         JOIN\n" +
            "     sys.tables tab ON col.object_id = tab.object_id\n" +
            "         JOIN\n" +
            "     sys.types typ ON col.system_type_id = typ.system_type_id\n" +
            "         LEFT JOIN (SELECT ic.column_id\n" +
            "                    FROM sys.indexes idx\n" +
            "                             JOIN\n" +
            "                         sys.index_columns ic ON idx.object_id = ic.object_id AND idx.index_id = ic.index_id\n" +
            "                    WHERE idx.is_primary_key = 1\n" +
            "                      AND idx.object_id = OBJECT_ID(?)) idx ON col.column_id = idx.column_id\n" +
            "WHERE tab.name = ?";

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

        return dbAccess.selectList(sql, DynamicColumn.class, table, table);
    }

    @Override
    public String getVersion() {
        String s = dbAccess.selectScalar("select @@version", String.class);
        return extractVersion(s);
    }


}