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

import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

/**
 * OpMeta
 * 获取数据库元数据信息
 * 全部交给jdbc驱动去实现
 * 这是通用实现，如果是特殊类型的数据库，或者需要覆盖驱动里面的实现，可以再加一个类同时实现IOpMeta接口
 * 同时在 easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta#excludeDbType 里面添加要排除的类型
 *
 * @author bokun.li
 * @date 2025-08-23
 * @see DialectV2
 */
@Slf4j
@Deprecated
public class OpDbMeta implements IOpMeta {

    private DialectV2 dialectV2;

    public static IOpMeta select(Connection connection) {
        DialectV2 dialectV2 = DialectFactory.get(connection);
        OpDbMeta opDbMeta = new OpDbMeta(connection);
        opDbMeta.setDialectV2(dialectV2);
        return opDbMeta;
    }

    public static OpDbMeta get(Connection connection) {
        return new OpDbMeta(connection);
    }
    public OpDbMeta(Connection connection) {
        this.dialectV2 = DialectFactory.get(connection);
    }

    public void setCloseConnection(boolean closeConnection) {
        dialectV2.setCloseConnection(closeConnection);
    }

    public void setDialectV2(DialectV2 dialectV2) {
        this.dialectV2 = dialectV2;
    }

    @Override
    public int getMajorVersion() {
        return dialectV2.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return dialectV2.getMinorVersion();
    }

    @Override
    public String getProductVersion() {
        return dialectV2.getProductVersion();
    }

    @Override
    public List<TableMetadata> getAllTableInfo() {
        return dialectV2.getAllTableInfo();
    }

    @Override
    public List<TableMetadata> getAllTableInfoByTableType(@Nullable String tableNamePattern, String[] tableType) {
        return dialectV2.getAllTableInfoByTableType(tableNamePattern,tableType);
    }

    @Override
    public List<TableMetadata> getTableInfos(String tableNamePattern) {
        return dialectV2.getTableInfos(tableNamePattern);
    }

    @Override
    public List<DatabaseColumnMetadata> getColumns(String catLog, String schema, String tableName) {
        return dialectV2.getColumns(catLog,schema,tableName);
    }

    @Override
    public List<DatabaseColumnMetadata> getColumnsNoCache(String catLog, String schema, String tableName) throws SQLException {
        return dialectV2.getColumnsNoCache(catLog,schema,tableName);

    }

    @Override
    public List<PrimaryKeyMetadata> getPrimaryKes(String catLog, String schema, String tableName) {
        return dialectV2.getPrimaryKes(catLog,schema,tableName);
    }

    @Override
    public List<IndexInfoMetaInfo> getIndexInfos(String catLog, String schema, String tableName) {
        return dialectV2.getIndexInfos(catLog,schema,tableName);
    }

    @Override
    public List<CatalogMetadata> getCataLogs() {
        return dialectV2.getCataLogs();
    }

    @Override
    public List<SchemaMetadata> getSchemas(String catLog) {
        return dialectV2.getSchemas(catLog);
    }

    @Override
    public String getDbType() {
        return dialectV2.getDbType();
    }
}
