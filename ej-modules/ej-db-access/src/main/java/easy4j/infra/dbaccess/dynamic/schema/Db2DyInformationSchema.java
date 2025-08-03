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
 * Db2DyInformationSchema
 * 暂时没条件来测这个，
 *
 * @author bokun.li
 * @date 2025-07-31 20:11:58
 */
public class Db2DyInformationSchema extends AbstractDyInformationSchema {


    public static final String sql = "SELECT \n" +
            "  col.COLNAME AS column_name,\n" +
            "  col.TYPENAME || \n" +
            "    CASE \n" +
            "      WHEN col.LENGTH > 0 AND col.TYPENAME NOT IN ('INTEGER', 'BIGINT', 'SMALLINT', 'DATE', 'TIME', 'TIMESTAMP') \n" +
            "        THEN '(' || CASE WHEN col.SCALE IS NULL THEN col.LENGTH ELSE col.LENGTH || ',' || col.SCALE END || ')' \n" +
            "      ELSE '' \n" +
            "    END AS data_type,\n" +
            "  CASE \n" +
            "    WHEN col.TYPENAME IN ('VARCHAR', 'CHAR', 'GRAPHIC', 'VARGRAPHIC') THEN col.LENGTH \n" +
            "    WHEN col.TYPENAME IN ('DECIMAL', 'NUMERIC', 'FLOAT', 'DOUBLE') THEN col.LENGTH \n" +
            "    ELSE NULL \n" +
            "  END AS str_max_len,\n" +
            "  CASE WHEN col.SCALE > 0 THEN col.SCALE ELSE NULL END AS number_precision,\n" +
            "  col.NULLS AS is_nullable,\n" +
            "  CASE WHEN key.COLNAME IS NOT NULL THEN 'PRI' ELSE '' END AS is_pre\n" +
            "FROM \n" +
            "  SYSCAT.COLUMNS col\n" +
            "LEFT JOIN SYSCAT.KEYCOLUSE key \n" +
            "  ON col.TABSCHEMA = key.TABSCHEMA \n" +
            "  AND col.TABNAME = key.TABNAME \n" +
            "  AND col.COLNAME = key.COLNAME\n" +
            "WHERE \n" +
            "  col.TABSCHEMA = ?\n" +
            "  AND col.TABNAME = ?";

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

    @Override
    public String getVersion() {
        String s = dbAccess.selectScalar("SELECT GETVARIABLE('SYSIBM.VERSION') FROM SYSIBM.SYSDUMMY1", String.class);
        return extractVersion(s);
    }
}