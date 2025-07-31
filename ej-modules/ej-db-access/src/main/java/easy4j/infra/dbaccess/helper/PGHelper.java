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
package easy4j.infra.dbaccess.helper;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.json.JacksonUtil;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

/**
 * PGHelper
 * handler pg type
 *
 * @author bokun.li
 * @date 2025/7/30
 */
public class PGHelper {
    public static String DB_TYPE;

    static {
        DB_TYPE = Easy4j.getDbType();
    }

    public static Object wrap(String type, Object value) {
        if(null==value) return value;
        if (DbType.POSTGRE_SQL.getDb().equals(DB_TYPE)) {
            PGobject pGobject = new PGobject();
            pGobject.setType(type);
            String json;
            try{
                if (JacksonUtil.isValidJson(value.toString())) {
                    json = value.toString();
                }else{
                    json = JacksonUtil.toJson(value);
                }
            }catch (Exception e){
                json = value.toString();
            }
            try {
                pGobject.setValue(json);
            } catch (SQLException e) {
                throw JdbcHelper.translateSqlException("saveListByBean", null, e);
            }
            return pGobject;
        }else{
            return value;
        }
    }

}
