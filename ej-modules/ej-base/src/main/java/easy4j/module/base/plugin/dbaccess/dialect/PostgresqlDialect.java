/* Copyright 2013-2015 www.snakerflow.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.base.plugin.dbaccess.dialect;


import easy4j.module.base.plugin.dbaccess.Page;

/**
 * Postgresql数据库方言实现
 */
public class PostgresqlDialect extends AbstractDialect {
	/**
	 * Postgresql分页通过limit实现
	 */
	public String getPageSql(String sql, Page<?> page) {
        return getPageBefore(sql, page) +
                sql +
                getPageAfter(sql, page);
	}

	
	public String getPageBefore(String sql, Page<?> page) {
		return "";
	}

	public String getPageAfter(String sql, Page<?> page) {
		int start = (page.getPageNo() - 1) * page.getPageSize();
        return " limit " + page.getPageSize() + " offset " + start;
	}
}
