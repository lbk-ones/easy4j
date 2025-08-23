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
package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import easy4j.infra.dbaccess.helper.JdbcHelper;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOpMeta
 *
 * @author bokun.li
 * @date 2025-08-23
 */
public interface IOpMeta {

    boolean match(Connection connection);

    /**
     * 返回唯一名称，可以返回空，如果是空那么就用 dbType来match
     * @return
     */
    String getName();

    /**
     * 获取数据库类型
     *
     * @param connection
     * @return
     * @see JdbcHelper#getDefaultDatabaseTypeMappings()
     */
    String getDbType(Connection connection);

    /**
     * 填充connection对象
     *
     * @param connection
     */
    void setConnection(Connection connection);

    /**
     * 主要版本号
     *
     * @return
     */
    int getMajorVersion();

    /**
     * 次要版本号
     *
     * @return
     */
    int getMinorVersion();

    /**
     * 版本号 可能有版本的详细描述信息
     *
     * @return
     */
    String getProductVersion();

    /**
     * 获取所有 表/视图 结构
     *
     * @return
     */
    List<TableMetadata> getAllTableInfo();

    /**
     * 根据表名称获取 表/视图 信息
     *
     * @param tableNamePattern
     * @return
     */
    List<TableMetadata> getTableInfos(String tableNamePattern);

    /**
     * 根据表名称获取 表/视图 中的字段信息
     *
     * @author bokun.li
     * @date 2025/8/23
     */
    List<DatabaseColumnMetadata> getColumns(String catLog, String schema, String tableName);

    /**
     * 根据表名称获取 表/视图 中的主键信息
     *
     * @author bokun.li
     * @date 2025/8/23
     */
    List<PrimaryKeyMetadata> getPrimaryKes(String catLog, String schema, String tableName);

    /**
     * 获取表索引信息
     * @param catLog
     * @param schema
     * @param tableName
     * @return
     */
    List<IndexInfoMetaInfo> getIndexInfos(String catLog, String schema, String tableName);

}
