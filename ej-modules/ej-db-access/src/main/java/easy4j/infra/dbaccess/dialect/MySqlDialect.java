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
package easy4j.infra.dbaccess.dialect;


import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import easy4j.infra.dbaccess.Page;

/**
 * Mysql数据库方言实现
 */
@Deprecated
public class MySqlDialect extends AbstractDialect {
    /**
     * mysql分页通过limit实现
     */
    public String getPageSql(String sql, Page<?> page) {
        StringBuilder pageSql = new StringBuilder(sql.length() + 100);
        pageSql.append(sql);
        int start = (page.getPageNo() - 1) * page.getPageSize();
        pageSql.append(" limit ").append(start).append(",").append(page.getPageSize());
        return pageSql.toString();
    }

    @Override
    public Wrapper getWrapper() {
        return new Wrapper('`');
    }

    @Override
    public String strDateToFunc(String str) {
        if (StrUtil.isNotBlank(str)) {
            return "CAST('" + str + "' AS DATETIME)";
        } else {
            return str;
        }
    }
}
