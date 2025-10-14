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

import cn.hutool.cache.impl.WeakCache;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.commons.dbutils.handlers.MapListHandler;

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

    public static final List<String> excludeDbType = ListTs.asList();
    private static final List<IOpMeta> iOpMetas = ListTs.newLinkedList();

    private final OpConfig opConfig = new OpConfig();


    // cache 30 minutes
    private static final WeakCache<Object, Object> dynamicColumnCache = new WeakCache<>(30 * 60 * 1000L);


    static {
        dynamicColumnCache.schedulePrune(30 * 60 * 1000L);
        iOpMetas.add(new OpDbMeta());
    }


    // 执行完毕释放连接 默认不释放
    private boolean isCloseConnection = false;

    private Connection connection;

    public static IOpMeta select(Connection connection) {
        for (IOpMeta iOpMeta : iOpMetas) {
            iOpMeta.setConnection(connection);
            if (iOpMeta.match(connection)) {
                return iOpMeta;
            }
        }
        String databaseType = "unknow db type";
        try {
            databaseType = JdbcHelper.getDatabaseType(connection);
        } catch (SQLException ignored) {
        }
        throw EasyException.wrap(BusCode.A00047, databaseType);
    }

    public static OpDbMeta get(Connection connection) {
        return new OpDbMeta(connection);
    }

    public OpDbMeta() {
    }

    public OpDbMeta(Connection connection) {
        this.connection = connection;
    }

    public OpDbMeta setCloseConnection(boolean closeConnection) {
        isCloseConnection = closeConnection;
        return this;
    }

    public String getCacheKeyFromConnection(Connection connection, String prefix) {
        String cachekey = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            cachekey = prefix + url + connection.getCatalog() + connection.getSchema();
        } catch (SQLException ignored) {

        }
        return cachekey;
    }

    @Override
    public String getDbType(Connection connection) {
        String cachekey = getCacheKeyFromConnection(connection, "getDbType-");
        return callback(() -> JdbcHelper.getDatabaseType(connection), cachekey, String.class);
    }

    @Override
    public boolean match(Connection connection) {
        String dbType = getDbType(connection);
        return !excludeDbType.contains(dbType);
    }

    @Override
    public String getName() {
        return "common";
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private void check() {
        CheckUtils.notNull(connection, "OpMeta of connection");
    }

    @Override
    public int getMajorVersion() {
        String cacheKey = getCacheKeyFromConnection(connection, "getMajorVersion-");
        return callback(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            return metaData.getDatabaseMajorVersion();
        }, cacheKey, int.class);
    }

    @Override
    public int getMinorVersion() {
        String cacheKey = getCacheKeyFromConnection(connection, "getMinorVersion-");
        return callback(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            return metaData.getDatabaseMinorVersion();
        }, cacheKey, int.class);
    }

    @Override
    public String getProductVersion() {
        String cacheKey = getCacheKeyFromConnection(connection, "getProductVersion-");
        return callback(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            return metaData.getDatabaseProductVersion();
        }, cacheKey, String.class);
    }

    @Override
    public List<TableMetadata> getAllTableInfo() {
        String cacheKey = getCacheKeyFromConnection(connection, "get-all-table-info-");
        return callbackList(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            String catalog = this.connection.getCatalog();
            String schema = this.connection.getSchema();
            ResultSet tables = metaData.getTables(catalog, schema, null, new String[]{"TABLE", "VIEW"});
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                List<TableMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, TableMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
                map = ListTs.distinct(map, TableMetadata::getTableName);
                return map;
            } finally {
                JdbcHelper.close(tables);
            }
        }, cacheKey, TableMetadata.class);
    }

    @Override
    public List<TableMetadata> getAllTableInfoByTableType(String tableNamePattern, String[] tableType) {
        if (ListTs.isEmpty(tableType)) return ListTs.newList();
        String cacheKey = getCacheKeyFromConnection(connection, "getAllTableInfoByTableType-" + tableNamePattern + Arrays.toString(tableType));
        return callbackList(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            String catalog = this.connection.getCatalog();
            String schema = this.connection.getSchema();
            ResultSet tables = metaData.getTables(catalog, schema, tableNamePattern, tableType);
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                List<TableMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, TableMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
                map = ListTs.distinct(map, TableMetadata::getTableName);
                return map;
            } finally {
                JdbcHelper.close(tables);
            }
        }, cacheKey, TableMetadata.class);
    }

    @Override
    public List<TableMetadata> getTableInfos(String tableNamePattern) {
        if (StrUtil.isBlank(tableNamePattern)) {
            return ListTs.newList();
        }
        var cacheKeyPrefix = "getTableInfos-" + tableNamePattern + "-";
        String cacheKey = getCacheKeyFromConnection(connection, cacheKeyPrefix);

        return callbackList(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            String catalog = this.connection.getCatalog();
            String schema = this.connection.getSchema();
            ResultSet tables = metaData.getTables(catalog, schema, tableNamePattern, new String[]{"TABLE", "VIEW"});
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                List<TableMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, TableMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
                map = ListTs.distinct(map, TableMetadata::getTableName);
                return map;
            } finally {
                JdbcHelper.close(tables);
            }
        }, cacheKey, TableMetadata.class);
    }

    @Override
    public List<DatabaseColumnMetadata> getColumns(String catLog, String schema, String tableName) {
        CheckUtils.notNull(tableName, "tableName");
        var cacheKeyPrefix = "getColumns-" + tableName;
        String cacheKey = getCacheKeyFromConnection(connection, cacheKeyPrefix);

        return callbackList(() -> getColumnsNoCache(catLog,schema,tableName), cacheKey, DatabaseColumnMetadata.class);
    }

    @Override
    public List<DatabaseColumnMetadata> getColumnsNoCache(String catLog, String schema, String tableName) throws SQLException {
        DatabaseMetaData metaData = this.connection.getMetaData();
        ResultSet tables = metaData.getColumns(catLog, schema, tableName, null);
        try {
            MapListHandler mapListHandler = new MapListHandler();
            List<Map<String, Object>> handle = mapListHandler.handle(tables);
            List<DatabaseColumnMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, DatabaseColumnMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
            map = ListTs.distinct(map, DatabaseColumnMetadata::getColumnName);
            return map;
        } finally {
            JdbcHelper.close(tables);
        }
    }

    @Override
    public List<PrimaryKeyMetadata> getPrimaryKes(String catLog, String schema, String tableName) {
        CheckUtils.notNull(tableName, "tableName");
        var cacheKeyPrefix = "getPrimaryKes-" + tableName;
        String cacheKey = getCacheKeyFromConnection(connection, cacheKeyPrefix);
        return callbackList(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            ResultSet tables = metaData.getPrimaryKeys(catLog, schema, tableName);
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                List<PrimaryKeyMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, PrimaryKeyMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
                map = ListTs.distinct(map, PrimaryKeyMetadata::getColumnName);
                return map;
            } finally {
                JdbcHelper.close(tables);
            }
        }, cacheKey, PrimaryKeyMetadata.class);
    }

    @Override
    public List<IndexInfoMetaInfo> getIndexInfos(String catLog, String schema, String tableName) {
        CheckUtils.notNull(tableName, "tableName");
        var cacheKeyPrefix = "getIndexInfos-" + tableName;
        String cacheKey = getCacheKeyFromConnection(connection, cacheKeyPrefix);
        return callbackList(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            // 返回近似值 可能查询效率更高
            ResultSet tables = metaData.getIndexInfo(catLog, schema, tableName, false, true);
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                return ListTs.map(handle, e -> BeanUtil.mapToBean(e, IndexInfoMetaInfo.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
            } finally {
                JdbcHelper.close(tables);
            }
        }, cacheKey, IndexInfoMetaInfo.class);
    }

    @Override
    public List<CatalogMetadata> getCataLogs() {
        var cacheKeyPrefix = "getCataLogs";
        String cacheKey = getCacheKeyFromConnection(connection, cacheKeyPrefix);
        return callbackList(() -> {
            String dbType = this.getDbType(connection);
            String sql = null;
            String sql2 = null;
            if (StrUtil.equalsIgnoreCase(dbType, DbType.H2.getDb())) {
                sql = "SELECT CATALOG_NAME FROM INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME ORDER BY CATALOG_NAME";
            } else if (StrUtil.equalsIgnoreCase(dbType, DbType.MYSQL.getDb())) {
                sql = "show databases where `database` not in ('information_schema','performance_schema','mysql','sys')";
            } else if (StrUtil.equalsIgnoreCase(dbType, DbType.ORACLE.getDb())) {
                sql = "SELECT USERNAME FROM DBA_USERS ORDER BY USERNAME";
                sql2 = "SELECT USERNAME FROM ALL_USERS ORDER BY USERNAME";
            } else if (StrUtil.equalsIgnoreCase(dbType, DbType.POSTGRE_SQL.getDb())) {
                sql = "SELECT datname FROM pg_database WHERE datistemplate = false and datname not in ('postgres')";
            } else if (StrUtil.equalsIgnoreCase(dbType, DbType.SQL_SERVER.getDb())) {
                sql = "SELECT name FROM sys.databases where name not in ('master','tempdb','model','msdb')";
            } else if (StrUtil.equalsIgnoreCase(dbType, DbType.DB2.getDb())) {
                sql = "SELECT NAME FROM SYSCAT.DATABASES ORDER BY NAME";
            }
            List<Map<String, Object>> handle = null;
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();
                MapListHandler mapListHandler = new MapListHandler();
                try {
                    handle = mapListHandler.handle(resultSet);
                } finally {
                    JdbcHelper.close(resultSet);
                }
            } catch (Exception e) {
                log.error("query appear exception " + e.getMessage());
            }
            if (ListTs.isEmpty(handle) && StrUtil.isNotBlank(sql2)) {
                ResultSet resultSet1 = connection.prepareStatement(sql2).executeQuery();
                try {
                    handle = new MapListHandler().handle(resultSet1);
                } finally {
                    JdbcHelper.close(resultSet1);
                }
            }
            List<CatalogMetadata> objects = ListTs.newList();
            for (Map<String, Object> stringObjectMap : handle) {
                Set<String> strings = stringObjectMap.keySet();
                Iterator<String> iterator = strings.iterator();
                String key = null;
                if (iterator.hasNext()) {
                    key = iterator.next();
                }
                if (null != key) {
                    CatalogMetadata catalogMetadata = new CatalogMetadata();
                    Object o = stringObjectMap.get(key);
                    String s = String.valueOf(o);
                    catalogMetadata.setTableCat(s);
                    objects.add(catalogMetadata);
                }
            }
            return objects;
        }, cacheKey, CatalogMetadata.class);
    }

    @Override
    public List<SchemaMetadata> getSchemas(String catLog) {
        boolean catLogIsNotEmpty = catLog != null && !catLog.isEmpty();
        var cacheKeyPrefix = "getSchemasBy" + catLog;
        String cacheKey = getCacheKeyFromConnection(connection, cacheKeyPrefix);
        return callbackList(() -> {
            List<SchemaMetadata> objects = ListTs.newList();
            String dbType = this.getDbType(connection);
            String sql;
            String escapeCn = opConfig.escapeCn(catLog, connection, false);
            String schemaName;
            String catlogName = null;
            // 根据数据库类型和指定的catalog构建查询SQL
            if (StrUtil.equals(dbType, DbType.MYSQL.getDb())) {
                // MySQL中catalog与database概念一致
                if (catLogIsNotEmpty) {
                    sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + escapeCn + "' ORDER BY SCHEMA_NAME";
                } else {
                    sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys') ORDER BY SCHEMA_NAME";
                }
                schemaName = "SCHEMA_NAME";

            } else if (StrUtil.equals(dbType, DbType.H2.getDb())) {
                if (catLogIsNotEmpty) {
                    sql = "SELECT SCHEMA_NAME,CATALOG_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE CATALOG_NAME = '" + escapeCn + "' AND SCHEMA_NAME NOT IN ('INFORMATION_SCHEMA', 'SYSTEM') ORDER BY SCHEMA_NAME";
                } else {
                    sql = "SELECT SCHEMA_NAME,CATALOG_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME NOT IN ('INFORMATION_SCHEMA', 'SYSTEM') ORDER BY SCHEMA_NAME";
                }
                schemaName = "SCHEMA_NAME";
                catlogName = "CATALOG_NAME";
            } else if (StrUtil.equals(dbType, DbType.ORACLE.getDb())) {
                // Oracle中没有catalog概念，直接查询所有schema
                if (catLogIsNotEmpty) {
                    try {
                        connection.createStatement().executeQuery("SELECT 1 FROM DBA_USERS");
                        sql = "SELECT USERNAME FROM DBA_USERS WHERE USERNAME NOT IN ('SYS', 'SYSTEM', 'OUTLN', 'SYSMAN', 'DBSNMP', 'APPQOSSYS') and USERNAME = '" + escapeCn + "' ORDER BY USERNAME";
                    } catch (SQLException e) {
                        sql = "SELECT USERNAME FROM ALL_USERS WHERE USERNAME NOT IN ('SYS', 'SYSTEM', 'OUTLN', 'SYSMAN', 'DBSNMP', 'APPQOSSYS') and USERNAME = '" + escapeCn + "' ORDER BY USERNAME";
                    }
                } else {
                    try {
                        connection.createStatement().executeQuery("SELECT 1 FROM DBA_USERS");
                        sql = "SELECT USERNAME FROM DBA_USERS WHERE USERNAME NOT IN ('SYS', 'SYSTEM', 'OUTLN', 'SYSMAN', 'DBSNMP', 'APPQOSSYS') ORDER BY USERNAME";
                    } catch (SQLException e) {
                        sql = "SELECT USERNAME FROM ALL_USERS WHERE USERNAME NOT IN ('SYS', 'SYSTEM', 'OUTLN', 'SYSMAN', 'DBSNMP', 'APPQOSSYS') ORDER BY USERNAME";
                    }
                }
                schemaName = "USERNAME";
            } else if (StrUtil.equals(dbType, DbType.POSTGRE_SQL.getDb())) {
                if (catLogIsNotEmpty) {
                    sql = "SELECT SCHEMA_NAME,CATALOG_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE CATALOG_NAME = '" + escapeCn + "' AND SCHEMA_NAME NOT IN ('information_schema', 'pg_catalog', 'pg_toast') AND SCHEMA_NAME NOT LIKE 'pg_temp_%' AND SCHEMA_NAME NOT LIKE 'pg_toast_temp_%' ORDER BY SCHEMA_NAME";
                } else {
                    sql = "SELECT SCHEMA_NAME,CATALOG_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME NOT IN ('information_schema', 'pg_catalog', 'pg_toast') AND SCHEMA_NAME NOT LIKE 'pg_temp_%' AND SCHEMA_NAME NOT LIKE 'pg_toast_temp_%' ORDER BY SCHEMA_NAME";
                }
                schemaName = "SCHEMA_NAME";
                catlogName = "CATALOG_NAME";
            } else if (StrUtil.equals(dbType, DbType.SQL_SERVER.getDb())) {
                if (catLogIsNotEmpty) {
                    sql = "SELECT name FROM " + escapeCn + ".sys.schemas WHERE name NOT IN ('sys', 'INFORMATION_SCHEMA') ORDER BY name";
                } else {
                    sql = "SELECT name FROM sys.schemas WHERE name NOT IN ('sys', 'INFORMATION_SCHEMA') ORDER BY name";
                }
                schemaName = "name";
            } else if (StrUtil.equals(dbType, DbType.DB2.getDb())) {
                if (catLogIsNotEmpty) {
                    sql = "SELECT SCHEMANAME,CATALOGNAME FROM SYSCAT.SCHEMAS WHERE CATALOGNAME = '" + escapeCn + "' AND SCHEMANAME NOT LIKE 'SYS%' AND SCHEMANAME NOT IN ('INFORMATION_SCHEMA') ORDER BY SCHEMANAME";
                } else {
                    sql = "SELECT SCHEMANAME,CATALOGNAME FROM SYSCAT.SCHEMAS WHERE SCHEMANAME NOT LIKE 'SYS%' AND SCHEMANAME NOT IN ('INFORMATION_SCHEMA') ORDER BY SCHEMANAME";
                }
                schemaName = "SCHEMANAME";
                catlogName = "CATALOGNAME";
            } else {
                throw new SQLException("不支持的数据库类型: " + dbType);
            }

            // 执行查询并处理结果
            try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(rs);
                for (Map<String, Object> stringObjectMap : handle) {
                    SchemaMetadata schemaMetadata = new SchemaMetadata();
                    if (StrUtil.isNotBlank(schemaName)) {
                        Object o = opConfig.getMatchMapIgnoreCase(stringObjectMap, schemaName);
                        if (o != null) {
                            String s = String.valueOf(o);
                            schemaMetadata.setSchema(s);
                        }
                    }
                    if (StrUtil.isNotBlank(catlogName)) {
                        Object o = opConfig.getMatchMapIgnoreCase(stringObjectMap, catlogName);
                        if (o != null) {
                            String s = String.valueOf(o);
                            schemaMetadata.setTableCat(s);
                        }
                    } else {
                        schemaMetadata.setTableCat(catLog);
                    }
                    objects.add(schemaMetadata);
                }


            }
            return objects;
        }, cacheKey, SchemaMetadata.class);
    }

    private <R> List<R> callbackList(OpCallBackList<R> consumer, Object cacheKey, Class<R> zclass) {
        if (null != cacheKey && zclass != null) {
            Object o = dynamicColumnCache.get(cacheKey);
            if (ObjectUtil.isNotEmpty(o)) {
                return Convert.toList(zclass, o);
            }
        }
        try {
            check();
            List<R> r = consumer.get();
            if (ObjectUtil.isNotEmpty(r) && ObjectUtil.isNotEmpty(cacheKey)) {
                dynamicColumnCache.put(cacheKey, r);
            }
            return r;
        } catch (SQLException sqlException) {
            throw JdbcHelper.translateSqlException("Opmeta callback " + cacheKey, null, sqlException);
        } finally {
            if (isCloseConnection) {
                // clear resource
                JdbcHelper.close(connection);
            }
        }
    }

    private <R> R callback(OpCallBack<R> consumer, Object cacheKey, Class<R> zclass) {
        if (null != cacheKey && zclass != null) {
            Object o = dynamicColumnCache.get(cacheKey);
            if (ObjectUtil.isNotEmpty(o)) {
                return Convert.convert(zclass, o);
            }
        }
        try {
            check();
            R r = consumer.get();
            if (ObjectUtil.isNotEmpty(r) && ObjectUtil.isNotEmpty(cacheKey)) {
                dynamicColumnCache.put(cacheKey, r);
            }
            return r;
        } catch (SQLException sqlException) {
            throw JdbcHelper.translateSqlException("Opmeta callback " + cacheKey, null, sqlException);
        } finally {
            if (isCloseConnection) {
                // clear resource
                JdbcHelper.close(connection);
            }
        }
    }

    @FunctionalInterface
    private interface OpCallBack<R> {

        R get() throws SQLException;
    }

    @FunctionalInterface
    private interface OpCallBackList<R> {

        List<R> get() throws SQLException;
    }
}
