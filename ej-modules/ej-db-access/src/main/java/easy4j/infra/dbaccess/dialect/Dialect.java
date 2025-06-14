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


import cn.hutool.db.sql.Wrapper;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.condition.WhereBuild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库差异的方言接口
 */
public interface Dialect {
    /**
     * 根据分页对象获取分页sql语句
     *
     * @param sql  未分页sql语句
     * @param page 分页对象
     * @return
     */
    String getPageSql(String sql, Page<?> page);

    /**
     * 转义字符
     *
     * @return
     */
    Wrapper getWrapper();

    /**
     * 批量写入
     *
     * @param tableName
     * @param columns
     * @param recordList
     * @param connection
     * @return
     * @throws SQLException
     */
    PreparedStatement psForBatchInsert(String tableName, String[] columns, List<Map<String, Object>> recordList, Connection connection);

    /**
     * 单个写入
     *
     * @param tableName
     * @param columns
     * @param record
     * @param connection
     * @return
     * @throws SQLException
     */
    PreparedStatement psForInsert(String tableName, String[] columns, Map<String, Object> record, Connection connection);

    /**
     * 根据主键更新
     *
     * @param tableName
     * @param recordList
     * @param aClass
     * @param ignoreNull
     * @param connection
     * @return
     * @throws SQLException
     */
    PreparedStatement psForUpdateById(String tableName, Object recordList, Class<?> aClass, boolean ignoreNull, Connection connection);

    /**
     * 根据条件单个更新
     *
     * @param tableName
     * @param record          要更新的bean(必须要是Map<String,Object>)
     * @param aClass          实体class
     * @param updateCondition 更新条件
     * @param ignoreNull      是否忽略空值
     * @param connection
     * @return
     * @throws SQLException
     */
    PreparedStatement psForUpdateBy(String tableName, Map<String, Object> record, Class<?> aClass, Map<String, Object> updateCondition, boolean ignoreNull, Connection connection);

    /**
     * 根据SqlBuild条件类更新
     *
     * @param tableName
     * @param record
     * @param whereBuilder
     * @param ignoreNull
     * @param connection
     * @return
     */
    PreparedStatement psForUpdateBySqlBuild(String tableName, Map<String, Object> record, WhereBuild whereBuilder, boolean ignoreNull, Connection connection);

    /**
     * 根据字符串条件来更新
     *
     * @param tableName
     * @param record
     * @param sqlBuilder
     * @param args
     * @param ignoreNull
     * @param connection
     * @return
     */
    PreparedStatement psForUpdateBySqlBuildStr(String tableName, Map<String, Object> record, String sqlBuilder, List<Object> args, boolean ignoreNull, Connection connection);

    /**
     * 多个批量更新
     *
     * @param tableName
     * @param columns
     * @param recordList
     * @param updateCondition
     * @param ignoreNull
     * @param connection
     * @return
     * @throws SQLException
     */
    PreparedStatement psForBatchUpdate(String tableName, String[] columns, List<Map<String, Object>> recordList, Map<String, Object> updateCondition, boolean ignoreNull, Connection connection);


    /**
     * 字符串转数据库时间类型
     * 输出函数与字符串组合
     *
     * @param str
     * @return
     */
    String strDateToFunc(String str);

    void printPrintLog(boolean isPrintLog);
}
