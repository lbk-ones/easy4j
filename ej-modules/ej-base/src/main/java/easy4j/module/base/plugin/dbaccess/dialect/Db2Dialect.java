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
import easy4j.module.base.plugin.dbaccess.Page;

/**
 * db2方言
 */
public class Db2Dialect extends AbstractDialect {
    public String getPageSql(String sql, Page<?> page) {
        StringBuilder pageSql = new StringBuilder(sql.length() + 100);
        pageSql.append("SELECT * FROM  ( SELECT B.*, ROWNUMBER() OVER() AS RN FROM ( ");
        pageSql.append(sql);
        int start = (page.getPageNo() - 1) * page.getPageSize() + 1;
        pageSql.append(" ) AS B )AS A WHERE A.RN BETWEEN ");
        pageSql.append(start);
        pageSql.append(" AND ");
        pageSql.append(start + page.getPageSize());
        return pageSql.toString();
    }

    @Override
    public String strDateToFunc(String str) {
        if (StrUtil.isNotBlank(str)) {
            return "TO_TIMESTAMP('" + str + "', 'YYYY-MM-DD HH24:MI:SS')";
        } else {
            return str;
        }
    }
}
