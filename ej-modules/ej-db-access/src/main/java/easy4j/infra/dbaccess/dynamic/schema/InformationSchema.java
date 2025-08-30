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

import cn.hutool.cache.impl.WeakCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.JdbcDbAccess;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta;
import easy4j.infra.dbaccess.helper.JdbcHelper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * InformationSchema
 * the InformationSchema is deprecated
 * please use easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta
 * @author bokun.li
 * @date 2025-07-31 21:30:34
 * @see OpDbMeta
 */
@Deprecated
public class InformationSchema {

    private static final Map<DataSource, String> dbTypeMap = Maps.newConcurrentMap();


    private static final Map<String, DyInformationSchema> informationSchemaMap = Maps.newConcurrentMap();

    // cache ten minutes
    private static final WeakCache<String, List<DynamicColumn>> dynamicColumnCache = new WeakCache<>(10 * 60 * 1000L);


    static {
        informationSchemaMap.put(DbType.H2.getDb(), new H2DyInformationSchema());
        informationSchemaMap.put(DbType.MYSQL.getDb(), new MysqlDyInformationSchema());
        informationSchemaMap.put(DbType.ORACLE.getDb(), new OracleDyInformationSchema());
        informationSchemaMap.put(DbType.POSTGRE_SQL.getDb(), new PgDyInformationSchema());
        informationSchemaMap.put(DbType.SQL_SERVER.getDb(), new SqlServerDyInformationSchema());
        informationSchemaMap.put(DbType.DB2.getDb(), new Db2DyInformationSchema());
    }

    public static void register(String name, DyInformationSchema dyInformationSchema) {
        informationSchemaMap.putIfAbsent(name, dyInformationSchema);
    }


    public static String getDbType(DataSource dataSource, Connection connection) throws SQLException {
        String s = dbTypeMap.get(dataSource);
        if (StrUtil.isBlank(s)) {
            String databaseType = null;
            databaseType = JdbcHelper.getDatabaseType(connection);
            s = databaseType;
            dbTypeMap.putIfAbsent(dataSource, databaseType);
        }
        return s;

    }

    public static String getDbVersion(DataSource dataSource, Connection connection) throws SQLException {
        DyInformationSchema dyInformationSchema = getDyInformationSchema(dataSource, connection);
        return dyInformationSchema.getVersion();
    }

    public static DBAccess getDbAccess(DataSource dataSource, Connection connection) throws SQLException {

        DyInformationSchema dyInformationSchema = getDyInformationSchema(dataSource, connection);

        return dyInformationSchema.getDbAccess();
    }

    private static DyInformationSchema getDyInformationSchema(DataSource dataSource, Connection connection) throws SQLException {
        String dbType = getDbType(dataSource, connection);

        DyInformationSchema dyInformationSchema = informationSchemaMap.get(dbType);

        if (null == dyInformationSchema) {
            throw new IllegalArgumentException("not support db type【" + dbType + "】 in dynamic information schema");
        }

        DBAccess dbAccess = dyInformationSchema.getDbAccess();

        if (null == dbAccess) {
            JdbcDbAccess jdbcDbAccess = new JdbcDbAccess();
            jdbcDbAccess.init(dataSource);
            dyInformationSchema.setDbAccess(jdbcDbAccess);
        }
        return dyInformationSchema;
    }

    public static List<DynamicColumn> getColumns(DataSource dataSource, String schema, String tableName, Connection connection) throws SQLException {
        List<DynamicColumn> dynamicColumns;
        Boolean property = Easy4j.getProperty(SysConstant.EASY4J_DB_ACCESS_NOT_CACHE_SCHEMA, Boolean.class, false);
        if (property) {
            // not cache
            DyInformationSchema dyInformationSchema = getDyInformationSchema(dataSource, connection);
            dynamicColumns = dyInformationSchema.getColumns(schema, tableName);
        } else {
            String s = (StrUtil.isBlank(schema) ? "" : schema + SP.DOT) + tableName;
            dynamicColumns = dynamicColumnCache.get(s);
            if (CollUtil.isEmpty(dynamicColumns)) {
                DyInformationSchema dyInformationSchema = getDyInformationSchema(dataSource, connection);
                dynamicColumns = dyInformationSchema.getColumns(schema, tableName);
                dynamicColumnCache.put(s, dynamicColumns);
            }
        }

        return dynamicColumns;
    }


}