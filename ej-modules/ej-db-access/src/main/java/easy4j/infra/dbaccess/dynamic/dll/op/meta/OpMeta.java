package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import cn.hutool.cache.impl.WeakCache;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.var;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * OpMeta
 * 全部交给jdbc驱动去实现
 *
 * @author bokun.li
 * @date 2025-08-23
 */
public class OpMeta implements IOpMeta {

    private Connection connection;

    private static final WeakCache<String, Object> dynamicColumnCache = new WeakCache<>(10 * 60 * 1000L);


    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private void check() {
        CheckUtils.notNull(connection, "OpMeta of connection");
    }

    @Override
    public int getMajorVersion() {
        var cacheKey = "getMajorVersion";
        Object o = dynamicColumnCache.get(cacheKey);
        if (o != null) {
            return Convert.toInt(o);
        }
        return callback(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            int databaseMajorVersion = metaData.getDatabaseMajorVersion();
            dynamicColumnCache.put(cacheKey, databaseMajorVersion);
            return databaseMajorVersion;
        });
    }

    @Override
    public int getMinorVersion() {
        var cacheKey = "getMinorVersion";
        Object o = dynamicColumnCache.get(cacheKey);
        if (o != null) {
            return Convert.toInt(o);
        }
        return callback(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            int databaseMinorVersion = metaData.getDatabaseMinorVersion();
            dynamicColumnCache.put(cacheKey, databaseMinorVersion);
            return databaseMinorVersion;
        });
    }

    @Override
    public String getProductVersion() {
        var cacheKey = "getProductVersion";
        Object o = dynamicColumnCache.get(cacheKey);
        if (o != null) {
            return Convert.toStr(o);
        }
        return callback(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            if (StrUtil.isNotBlank(databaseProductVersion)) {
                dynamicColumnCache.put(cacheKey, databaseProductVersion);
            }
            return databaseProductVersion;
        });
    }

    @Override
    public List<TableMetadata> getAllTableInfo() {
        var cacheKey = "all-table-info";
        Object o = dynamicColumnCache.get(cacheKey);
        if (o != null) {
            return Convert.toList(TableMetadata.class, o);
        }
        return callback(() -> {
            DatabaseMetaData metaData = this.connection.getMetaData();
            String catalog = this.connection.getCatalog();
            String schema = this.connection.getSchema();
            ResultSet tables = metaData.getTables(catalog, schema, null, new String[]{"TABLE", "VIEW"});
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                List<TableMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, TableMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
                if (ListTs.isNotEmpty(map)) {
                    dynamicColumnCache.put(cacheKey, map);
                }
                return map;
            } finally {
                JdbcHelper.close(tables);
            }
        });
    }

    @Override
    public List<TableMetadata> getTableInfos(String tableNamePattern) {
        if (StrUtil.isBlank(tableNamePattern)) {
            return ListTs.newList();
        }
        return callback(() -> {
            var cacheKey = "getTableInfos-" + tableNamePattern;
            Object o = dynamicColumnCache.get(cacheKey);
            if (o != null) {
                return Convert.toList(TableMetadata.class, o);
            }
            DatabaseMetaData metaData = this.connection.getMetaData();
            String catalog = this.connection.getCatalog();
            String schema = this.connection.getSchema();
            ResultSet tables = metaData.getTables(catalog, schema, tableNamePattern, new String[]{"TABLE", "VIEW"});
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                List<TableMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, TableMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
                if (CollUtil.isNotEmpty(map)) {
                    dynamicColumnCache.put(cacheKey, map);
                }
                return map;
            } finally {
                JdbcHelper.close(tables);
            }
        });
    }

    @Override
    public List<DatabaseColumnMetadata> getColumns(String catLog, String schema, String tableName) {
        CheckUtils.notNull(tableName, "tableName");
        return callback(() -> {
            var cacheKey = "getColumns-" + catLog + schema + tableName;
            Object o = dynamicColumnCache.get(cacheKey);
            if (o != null) {
                return Convert.toList(DatabaseColumnMetadata.class, o);
            }
            DatabaseMetaData metaData = this.connection.getMetaData();
            ResultSet tables = metaData.getColumns(catLog, schema, tableName, null);
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                List<DatabaseColumnMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, DatabaseColumnMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
                if (CollUtil.isNotEmpty(map)) {
                    dynamicColumnCache.put(cacheKey, map);
                }
                return map;
            } finally {
                JdbcHelper.close(tables);
            }
        });
    }

    @Override
    public List<PrimaryKeyMetadata> getPrimaryKes(String catLog, String schema, String tableName) {
        CheckUtils.notNull(tableName, "tableName");
        return callback(() -> {
            var cacheKey = "getColumns-" + catLog + schema + tableName;
            Object o = dynamicColumnCache.get(cacheKey);
            if (o != null) {
                return Convert.toList(PrimaryKeyMetadata.class, o);
            }
            DatabaseMetaData metaData = this.connection.getMetaData();
            ResultSet tables = metaData.getPrimaryKeys(catLog, schema, tableName);
            try {
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(tables);
                List<PrimaryKeyMetadata> map = ListTs.map(handle, e -> BeanUtil.mapToBean(e, PrimaryKeyMetadata.class, true, CopyOptions.create().ignoreCase().ignoreNullValue()));
                if (CollUtil.isNotEmpty(map)) {
                    dynamicColumnCache.put(cacheKey, map);
                }
                return map;
            } finally {
                JdbcHelper.close(tables);
            }
        });
    }

    private <R> R callback(OpCallBack<R> consumer) {
        try {
            check();
            return consumer.get();
        } catch (SQLException sqlException) {
            throw JdbcHelper.translateSqlException("Opmeta callback", null, sqlException);
        } finally {
            // clear resource
            //JdbcUtils.closeConnection(connection);
        }
    }

    @FunctionalInterface
    private interface OpCallBack<R> {

        R get() throws SQLException;
    }
}
