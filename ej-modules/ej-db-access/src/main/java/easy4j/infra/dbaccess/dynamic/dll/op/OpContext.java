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
package easy4j.infra.dbaccess.dynamic.dll.op;

import easy4j.infra.common.annotations.Desc;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.DatabaseColumnMetadata;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * OpContext
 * op的上下文
 *
 * @author bokun.li
 * @date 2025/8/23
 */
@Data
@Accessors(chain = true)
public class OpContext {

    // 数据库类型
    private String dbType;

    // 数据库版本
    private String dbVersion;

    // mysql拿catalog当schema 其他数据库不一定是这个
    private String connectionCatalog;

    // 这个不一定有值 看驱动实现
    private String connectionSchema;

    // 数据库中已有的列信息
    private List<DatabaseColumnMetadata> dbColumns;

    // 数据库中已有的列信息
    @Desc("需要新增的列")
    private List<DatabaseColumnMetadata> adColumns;

    // 传入的schema信息
    private String schema;

    // 表名称
    private String tableName;

    // 传入的数据源信息
    private DataSource dataSource;

    // 获取的全局连接
    private Connection connection;

    // 解析出来的数据库方言
    private Dialect dialect;

    // 解析出来的表元数据
    private DDLTableInfo ddlTableInfo;

    // parse java 才有这个字段
    private Class<?> domainClass;

    // 全局配置
    private OpConfig opConfig;


}
