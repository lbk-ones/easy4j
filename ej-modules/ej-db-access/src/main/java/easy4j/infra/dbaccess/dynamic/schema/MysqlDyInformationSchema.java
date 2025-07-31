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

/**
 * MysqlDyInformationSchema
 *
 * @author bokun.li
 * @date 2025-07-31 20:11:58
 */
public class MysqlDyInformationSchema extends AbstractDyInformationSchema {


    public static final String sql = "SELECT\n" +
            "  column_name AS column_name,\n" +
            "  data_type AS data_type,\n" +
            "  character_maximum_length as str_max_len,\n" +
            "  numeric_precision as number_precision,\n" +
            "  is_nullable AS is_nullable,\n" +
            "  (case column_key\n" +
            "      when 'PRI' then 'PRI' else '' end\n" +
            "  ) AS is_pre\n" +
            "FROM\n" +
            "  information_schema.columns\n" +
            "WHERE\n" +
            "  table_schema = ?\n" +
            "  AND table_name = ?";

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

        return dbAccess.selectList(sql, DynamicColumn.class, schema, table);
    }
}