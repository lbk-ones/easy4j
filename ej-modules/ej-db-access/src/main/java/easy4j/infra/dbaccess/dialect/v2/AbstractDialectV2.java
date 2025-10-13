package easy4j.infra.dbaccess.dialect.v2;

import cn.hutool.cache.impl.WeakCache;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import cn.hutool.db.sql.Wrapper;
import com.google.common.collect.Maps;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.RegexEscapeUtils;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.dynamic.dll.op.DBFieldEscapeChecker;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.*;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.var;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public abstract class AbstractDialectV2 extends CommonDBAccess implements DialectV2 {

    public Logger log = LoggerFactory.getLogger(this.getClass());

    private static final List<String> ORACLE_ESCAPE = ListTs.asList("or", "decimal", "create", "from", "public", "union", "nowait", "raw", "to", "pctfree", "values", "default", "grant", "with", "table", "alter", "<", "select", "varchar", "any", "|", "-", "group", "identified", "/", "^", "null", "connect", "view", "distinct", "set", "by", "order", "minus", "prior", "asc", "varchar2", "all", "+", "drop", "and", "lock", "intersect", "having", "on", "update", "between", "exists", ":", "integer", "insert", "for", "char", "smallint", "=", "mode", "revoke", "else", ">", "in", "rename", "trigger", "number", "synonym", ".", "cluster", "start", "share", "of", "option", "into", "compress", "where", "*", "check", "then", "as", "[", "unique", "]", "@", ",", "long", "size", "(", "delete", "not", ")", "desc", "date", "resource", "float", "is", "like", "exclusive", "&", "!", "nocompress", "index", "null");
    private static final List<String> PG_ESCAPE = ListTs.asList("all", "analyse", "analyze", "and", "any", "array", "as", "asc", "asymmetric", "both", "case", "cast", "check", "collate", "column", "constraint", "create", "current_catalog", "current_date", "current_role", "current_time", "current_timestamp", "current_user", "default", "deferrable", "desc", "distinct", "do", "else", "end", "except", "false", "fetch", "for", "foreign", "from", "grant", "group", "having", "in", "initially", "intersect", "into", "lateral", "leading", "limit", "localtime", "localtimestamp", "not", "null", "offset", "on", "only", "or", "order", "placing", "primary", "references", "returning", "select", "session_user", "some", "symmetric", "table", "then", "to", "trailing", "true", "union", "unique", "user", "using", "variadic", "when", "where", "window", "with");
    private static final List<String> MYSQL_ESCAPE = ListTs.asList("accessible", "add", "all", "alter", "analyze", "and", "as", "asc", "asensitive", "before", "between", "bigint", "binary", "blob", "both", "by", "call", "cascade", "case", "change", "char", "character", "check", "collate", "column", "condition", "constraint", "continue", "convert", "create", "cross", "cube", "cume_dist", "current_date", "current_time", "current_timestamp", "current_user", "cursor", "database", "databases", "day_hour", "day_microsecond", "day_minute", "day_second", "dec", "decimal", "declare", "default", "delayed", "delete", "dense_rank", "desc", "describe", "deterministic", "distinct", "distinctrow", "div", "double", "drop", "dual", "each", "else", "elseif", "empty", "enclosed", "escaped", "except", "exists", "exit", "explain", "false", "fetch", "first_value", "float", "float4", "float8", "for", "force", "foreign", "from", "fulltext", "function", "generated", "get", "grant", "group", "grouping", "groups", "having", "high_priority", "hour_microsecond", "hour_minute", "hour_second", "if", "ignore", "in", "index", "infile", "inner", "inout", "insensitive", "insert", "int", "int1", "int2", "int3", "int4", "int8", "integer", "intersect", "interval", "into", "io_after_gtids", "io_before_gtids", "is", "iterate", "join", "json_table", "key", "keys", "kill", "lag", "last_value", "lateral", "lead", "leading", "leave", "left", "like", "limit", "linear", "lines", "load", "localtime", "localtimestamp", "lock", "long", "longblob", "longtext", "loop", "low_priority", "master_bind", "master_ssl_verify_server_cert", "match", "maxvalue", "mediumblob", "mediumint", "mediumtext", "middleint", "minute_microsecond", "minute_second", "mod", "modifies", "natural", "not", "no_write_to_binlog", "nth_value", "ntile", "null", "numeric", "of", "on", "optimize", "optimizer_costs", "option", "optionally", "or", "order", "out", "outer", "outfile", "over", "partition", "percent_rank", "precision", "primary", "procedure", "purge", "range", "rank", "read", "reads", "read_write", "real", "recursive", "references", "regexp", "release", "rename", "repeat", "replace", "require", "resignal", "restrict", "return", "revoke", "right", "rlike", "row", "rows", "row_number", "schema", "schemas", "second_microsecond", "select", "sensitive", "separator", "set", "show", "signal", "smallint", "spatial", "specific", "sql", "sqlexception", "sqlstate", "sqlwarning", "sql_big_result", "sql_calc_found_rows", "sql_small_result", "ssl", "starting", "stored", "straight_join", "system", "table", "terminated", "then", "tinyblob", "tinyint", "tinytext", "to", "trailing", "trigger", "true", "undo", "union", "unique", "unlock", "unsigned", "update", "usage", "use", "using", "utc_date", "utc_time", "utc_timestamp", "values", "varbinary", "varchar", "varcharacter", "varying", "virtual", "when", "where", "while", "window", "with", "write", "xor", "year_month", "zerofill");
    private static final List<String> H2_ESCAPE = ListTs.asList("all", "and", "any", "array", "as", "asymmetric", "authorization", "between", "both", "case", "cast", "check", "constraint", "cross", "current_catalog", "current_date", "current_path", "current_role", "current_schema", "current_time", "current_timestamp", "current_user", "day", "default", "distinct", "else", "end", "except", "exists", "false", "fetch", "for", "foreign", "from", "full", "group", "groups", "having", "hour", "if", "ilike", "in", "inner", "intersect", "interval", "is", "join", "key", "leading", "left", "like", "limit", "localtime", "localtimestamp", "minus", "minute", "month", "natural", "not", "null", "offset", "on", "or", "order", "over", "partition", "primary", "qualify", "range", "regexp", "right", "row", "rownum", "rows", "second", "select", "session_user", "set", "some", "symmetric", "system_user", "table", "to", "top", "ms", "cs", "trailing", "true", "uescape", "union", "unique", "unknown", "user", "using", "value", "values", "when", "where", "window", "with", "year", "_rowid_");
    private static final List<String> MSSQL_ESCAPE = ListTs.asList("add", "external", "procedure", "all", "fetch", "public", "alter", "file", "raiserror", "and", "fillfactor", "read", "any", "for", "readtext", "as", "foreign", "reconfigure", "asc", "freetext", "references", "authorization", "freetexttable", "replication", "backup", "from", "restore", "begin", "full", "restrict", "between", "function", "return", "break", "goto", "revert", "browse", "grant", "revoke", "bulk", "group", "right", "by", "having", "rollback", "cascade", "holdlock", "rowcount", "case", "identity", "rowguidcol", "check", "identity_insert", "rule", "checkpoint", "identitycol", "save", "close", "if", "schema", "clustered", "in", "securityaudit", "coalesce", "index", "select", "collate", "inner", "semantickeyphrasetable", "column", "insert", "semanticsimilaritydetailstable", "commit", "intersect", "semanticsimilaritytable", "compute", "into", "session_user", "constraint", "is", "set", "contains", "join", "setuser", "containstable", "key", "shutdown", "continue", "kill", "some", "convert", "left", "statistics", "create", "like", "system_user", "cross", "lineno", "table", "current", "load", "tablesample", "current_date", "merge", "textsize", "current_time", "national", "then", "current_timestamp", "nocheck", "to", "current_user", "nonclustered", "top", "cursor", "not", "tran", "database", "null", "transaction", "dbcc", "nullif", "trigger", "deallocate", "of", "truncate", "declare", "off", "try_convert", "default", "offsets", "tsequal", "delete", "on", "union", "deny", "open", "unique", "desc", "opendatasource", "unpivot", "disk", "openquery", "update", "distinct", "openrowset", "updatetext", "distributed", "openxml", "use", "double", "option", "user", "drop", "or", "values", "dump", "order", "varying", "else", "outer", "view", "end", "over", "waitfor", "errlvl", "percent", "when", "escape", "pivot", "where", "except", "plan", "while", "exec", "precision", "with", "execute", "primary", "within group", "exists", "print", "writetext", "exit", "proc");


    private final OpConfig opConfig = new OpConfig();


    // cache 30 minutes
    private static final WeakCache<Object, Object> dynamicColumnCache = new WeakCache<>(30 * 60 * 1000L);

    public static Map<String, cn.hutool.db.sql.Wrapper> dbVsWrapper = Maps.newHashMap();

    static {
        dynamicColumnCache.schedulePrune(30 * 60 * 1000L);
        dbVsWrapper.put(DbType.MYSQL.getDb(), new cn.hutool.db.sql.Wrapper('`', '`'));
        dbVsWrapper.put(DbType.ORACLE.getDb(), new cn.hutool.db.sql.Wrapper('"', '"'));
        dbVsWrapper.put(DbType.H2.getDb(), new cn.hutool.db.sql.Wrapper('"', '"'));
        dbVsWrapper.put(DbType.POSTGRE_SQL.getDb(), new cn.hutool.db.sql.Wrapper('"', '"'));
        dbVsWrapper.put(DbType.SQL_SERVER.getDb(), new cn.hutool.db.sql.Wrapper('[', ']'));
        dbVsWrapper.put(DbType.DB2.getDb(), new Wrapper('"', '"'));
        dbVsWrapper = Collections.unmodifiableMap(dbVsWrapper);
    }


    // 执行完毕释放连接 默认不释放
    private boolean isCloseConnection = false;

    private final Connection connection;

    public AbstractDialectV2(Connection connection) {
        this.connection = connection;
    }

    public static OpDbMeta get(Connection connection) {
        return new OpDbMeta(connection);
    }


    public AbstractDialectV2 setCloseConnection(boolean closeConnection) {
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
    public String getDbType() {
        String cachekey = getCacheKeyFromConnection(connection, "getDbType-");
        return callback(() -> JdbcHelper.getDatabaseType(connection), cachekey, String.class);
    }

    @Override
    public String getConnectionCatalog() {
        try {
            return this.connection.getCatalog();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getConnectionSchema() {
        try {
            return this.connection.getSchema();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        return callbackList(() -> getColumnsNoCache(catLog, schema, tableName), cacheKey, DatabaseColumnMetadata.class);
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
            String dbType = this.getDbType();
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
            String dbType = this.getDbType();
            String sql;
            String escapeCn = escape(catLog);
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

    /**
     * 校验字符是否为英文字母（大写或小写）
     *
     * @param c 要校验的字符
     * @return 如果是英文字母返回 true，否则返回 false
     */
    public boolean isEnglishLetter(char c) {
        // 检查是否是大写字母 (A-Z) 或小写字母 (a-z)
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    public boolean containUpper(String name) {
        char[] charArray = name.toCharArray();
        for (char c : charArray) {
            if (isEnglishLetter(c) && StrUtil.isUpperCase(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }

    public boolean containLower(String name) {
        char[] charArray = name.toCharArray();
        for (char c : charArray) {
            if (isEnglishLetter(c) && StrUtil.isLowerCase(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String escape(String name) {
        String databaseType = this.getDbType();
        lbk:
        {
            // 先大概检查一下肯定需要转义的名称 不考虑数据库保留字
            // 如果强制转义那么也跳过
            if (DBFieldEscapeChecker.needEscape(name)) {
                break lbk;
            }
            // 这些数据库 只转义该转义的 其他不转义
            if (DbType.ORACLE.getDb().equals(databaseType) && !containLower(name)) {
                if (!ListTs.equalIgnoreCase(ORACLE_ESCAPE, name)) {
                    return name;
                }
            } else if (DbType.POSTGRE_SQL.getDb().equals(databaseType) && !containUpper(name)) {
                if (!ListTs.equalIgnoreCase(PG_ESCAPE, name)) {
                    return name;
                }
            } else if (DbType.MYSQL.getDb().equals(databaseType)) {
                if (!ListTs.equalIgnoreCase(MYSQL_ESCAPE, name)) {
                    return name;
                }
            } else if (DbType.H2.getDb().equals(databaseType) && !containLower(name)) {
                if (!ListTs.equalIgnoreCase(H2_ESCAPE, name)) {
                    return name;
                }
            } else if (DbType.SQL_SERVER.getDb().equals(databaseType)) {
                if (!ListTs.equalIgnoreCase(MSSQL_ESCAPE, name)) {
                    return name;
                }
            }
        }
        Wrapper wrapper = dbVsWrapper.getOrDefault(databaseType, new Wrapper());

        return wrapper.wrap(name);
    }

    @Override
    public String splitEscape(String name, String comma) {
        if (StrUtil.isBlank(name) || StrUtil.isBlank(comma)) return name;
        String s = RegexEscapeUtils.escapeRegex(comma);
        String[] split = name.split(s);
        List<String> list = ListTs.asList(split);
        return list.stream().map(this::escape).collect(Collectors.joining(comma));
    }

    @Override
    public String forceEscape(String name) {
        if (StrUtil.isBlank(name)) return name;
        return dbVsWrapper.getOrDefault(this.getDbType(), new Wrapper()).wrap(name);
    }

    @Override
    public String unescape(String name) {
        Wrapper orDefault = dbVsWrapper.getOrDefault(this.getDbType(), new Wrapper());
        try {
            char preWrapQuote = orDefault.getPreWrapQuote();
            char sufWrapQuote = orDefault.getSufWrapQuote();
            return StrUtil.unWrap(name, preWrapQuote, sufWrapQuote);
        } catch (Exception ignored) {
        }
        return name;
    }

    @Override
    public String splitUnescape(String name, String comma) {
        if (StrUtil.isBlank(name) || StrUtil.isBlank(comma)) return name;
        String s = RegexEscapeUtils.escapeRegex(comma);
        String[] split = name.split(s);
        List<String> list = ListTs.asList(split);
        return list.stream().map(this::unescape).collect(Collectors.joining(comma));
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------


    public String transferTemplate(Map<String, Object> params, String template) {
        if (StrUtil.isEmpty(template) || ListTs.isEmpty(params)) return "";
        for (String s : params.keySet()) {
            Object o = params.get(s);
            if (o != null) {
                template = template.replaceAll("\\[" + s + "]", o.toString());
            }
        }
        String result = template.replaceAll("\\[.*?]", " ");
        return result.replaceAll("\\s+", " ");
    }


    public String getJdbcBatchUpdateSqlTemplate() {
        // "update [TABLE_NAME] set [VALUES] where [WHERE]"
        return "update [TABLE_NAME] set [VALUES] where [WHERE]";
    }

    public String getJdbcDeleteSqlTemplate() {
        // "update [TABLE_NAME] set [VALUES] where [WHERE]"
        return "delete from [TABLE_NAME] where [WHERE]";
    }

    public String transferJdbcBatchUpdate(Map<String, Object> params) {
        return transferTemplate(params, getJdbcBatchUpdateSqlTemplate());
    }

    public String transferJdbcDelete(Map<String, Object> params) {
        return transferTemplate(params, getJdbcDeleteSqlTemplate());
    }

    public String getJdbcBatchInsertSqlTemplate() {
        return "insert into [TABLE_NAME] [COLUMNS] values [VALUES]";
    }

    public String transferJdbcBatch(Map<String, Object> params) {
        return transferTemplate(params, getJdbcBatchInsertSqlTemplate());
    }

    /**
     * 多条数据以jdbcBatch的方式批量写入
     * 单条则正常执行
     *
     * @param record      传入要写入的数据map
     * @param tableName   表名
     * @param schema      schema
     * @param batchSize   每次批量的大小
     * @param toUnderLine 将参数转为下划线
     * @param isCommit    是否直接提交事务
     * @return
     */
    @Override
    public PsResult jdbcInsert(
            List<Map<String, Object>> record,
            String tableName,
            String schema,
            int batchSize,
            boolean toUnderLine,
            boolean isCommit
    ) {
        CheckUtils.notNull(tableName);
        CheckUtils.notNull(record);
        schema = schema == null ? getConnectionSchema() : schema;
        tableName = escape(tableName);
        PsResult psResult = new PsResult();
        if (ListTs.isEmpty(record)) return psResult;
        batchSize = batchSize <= 0 ? 200 : batchSize;
        Set<String> columnNameList = new HashSet<>();
        List<DatabaseColumnMetadata> columns = this.getColumns(getConnectionCatalog(), schema, tableName);

        List<String> autoKey = columns.stream().filter(e -> {
            String isAutoincrement = e.getIsAutoincrement();
            return "YES".equals(isAutoincrement);
        }).map(DatabaseColumnMetadata::getColumnName).collect(Collectors.toList());

        Map<String, DatabaseColumnMetadata> map = ListTs.toMap(columns, DatabaseColumnMetadata::getColumnName);
        List<Map<String, Object>> newList = ListTs.newLinkedList();
        for (Map<String, Object> recordMap : record) {
            Map<String, Object> newMap = new HashMap<>();
            Set<String> strings = recordMap.keySet();
            for (String column : strings) {
                // differ with db columns
                DatabaseColumnMetadata databaseColumnMetadata = opConfig.getMatchMapIgnoreCase(map, column);
                if (databaseColumnMetadata == null) {
                    databaseColumnMetadata = opConfig.getMatchMapIgnoreCase(map, StrUtil.toUnderlineCase(column));
                    if (databaseColumnMetadata == null) {
                        continue;
                    }
                }
                Object o = recordMap.get(column);
                if (toUnderLine) {
                    column = StrUtil.toUnderlineCase(column);
                }
                column = escape(column);
                columnNameList.add(column);
                newMap.put(column, o);
            }
            if (ListTs.isNotEmpty(newMap)) {
                newList.add(newMap);
            }
        }
        if (ListTs.isNotEmpty(newList)) {
            boolean oneRow = newList.size() == 1;
            List<String> strings = new ArrayList<>(columnNameList);
            Map<String, Object> params = Maps.newHashMap();
            params.put("TABLE_NAME", String.join(SP.DOT, ListTs.asNonNullList(schema, tableName)));
            params.put("COLUMNS_LIST", strings);
            params.put("COLUMNS", "(" + ListTs.join(",", strings) + ")");
            params.put("VALUES", "(" + ListTs.join(",", ListTs.map(strings, e -> "?")) + ")");
            params.put("VALUES_ZWF", ListTs.join(",", ListTs.map(strings, e -> "?")));
            String sql = transferJdbcBatch(params);
            PreparedStatement preparedStatement = null;
            ResultSet generatedKeys = null;
            try {
                long beginTime = System.currentTimeMillis();
                boolean oldCommit = this.connection.getAutoCommit();
                if (isCommit) this.connection.setAutoCommit(false);
                preparedStatement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                List<List<Map<String, Object>>> partition = ListTs.partition(newList, batchSize);
                int effectRows = 0;
                List<String> sqls = ListTs.newList();
                for (List<Map<String, Object>> maps : partition) {
                    for (Map<String, Object> stringObjectMap : maps) {
                        effectRows++;
                        List<Object> objects = ListTs.newList();
                        for (String string : strings) {
                            Object valuye = stringObjectMap.get(string);
                            objects.add(valuye);
                        }
                        Object[] array = objects.toArray();
                        StatementUtil.fillParams(preparedStatement, array);
                        if (!oneRow) {
                            preparedStatement.addBatch();
                        }
                        Pair<String, Date> stringDatePair = recordSql(sql, connection, array);
                        if (stringDatePair != null) sqls.add(stringDatePair.getKey());
                    }
                    if (!oneRow) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                    }
                }
                if (oneRow) {
                    effectRows = preparedStatement.executeUpdate();
                }
                // return generatedKeys
                generatedKeys = preparedStatement.getGeneratedKeys();
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(generatedKeys);
                for (int i = 0; i < handle.size(); i++) {
                    try {
                        Map<String, Object> stringObjectMap = record.get(i);
                        if (ListTs.isNotEmpty(stringObjectMap)) {
                            Map<String, Object> resultHandler = handle.get(i);
                            if (ListTs.isNotEmpty(resultHandler)) {
                                int s_index = 0;
                                for (String s : resultHandler.keySet()) {
                                    String autokeyName = ListTs.get(autoKey, s_index);
                                    if (null != autokeyName) {
                                        resultHandler.put(autokeyName, resultHandler.get(s));
                                    }
                                    resultHandler.put(s, resultHandler.get(s));
                                    s_index++;
                                }
                                stringObjectMap.putAll(resultHandler);
                            }
                        }
                    } catch (Exception e) {
                        log.error("jdbc batch insert appear error:" + e.getMessage());
                    }
                }
                long endTime = System.currentTimeMillis();
                if (isCommit) {
                    this.connection.commit();
                    this.connection.setAutoCommit(oldCommit);
                }
                psResult.setSql(sqls);
                psResult.setCostTime(endTime - beginTime);
                psResult.setEffectRows(effectRows);
            } catch (SQLException e) {
                if (isCommit) {
                    try {
                        this.connection.rollback();
                    } catch (SQLException ex) {
                        throw JdbcHelper.translateSqlException("jdbcInsert rollback", "", e);
                    }
                }
                throw JdbcHelper.translateSqlException("jdbcInsert", "", e);
            } finally {
                JdbcHelper.close(generatedKeys);
                JdbcHelper.close(preparedStatement);
            }
        }
        return psResult;
    }

    /**
     * jdbc方式的批量很难做一个真正的批量更新，因为如果要做真正的批量更新，那么多条数据的更新sql必须一样，但是更新的话很可能每条数据的sql不一样，那么就很尴尬，所以这里要做一个取舍，当然不是说不能做，而是单纯以batch的方式很难
     *
     * @param record             传入要写入的数据map
     * @param tableName          表名
     * @param schema             schema
     * @param skipNotExistsField 跳过不存在的字段
     * @param toUnderLine        将参数转为下划线
     * @param skipUpdateNull     跳过更新null值
     * @param isCommit           是否直接提交事务
     * @param whereBuild         条件构造器
     * @return
     */
    @Override
    public PsResult jdbcUpdate(
            List<Map<String, Object>> record,
            String tableName,
            String schema,
            boolean skipNotExistsField,
            boolean toUnderLine,
            boolean skipUpdateNull,
            boolean isCommit,
            WhereBuild whereBuild
    ) {
        CheckUtils.notNull(tableName, "tableName");
        CheckUtils.notNull(record, "record");
        CheckUtils.notNull(whereBuild, "whereBuild");
        whereBuild.bind(connection);
        whereBuild.setToUnderLine(toUnderLine);
        schema = schema == null ? getConnectionSchema() : schema;
        tableName = escape(tableName);
        PsResult psResult = new PsResult();
        if (ListTs.isEmpty(record)) return psResult;
        Map<String, DatabaseColumnMetadata> map = null;
        if (skipNotExistsField) {
            List<DatabaseColumnMetadata> columns = this.getColumns(getConnectionCatalog(), schema, tableName);
            map = ListTs.toMap(columns, DatabaseColumnMetadata::getColumnName);
        }
        try {
            long beginTime = System.currentTimeMillis();
            boolean autoCommit = this.connection.getAutoCommit();
            if (isCommit) this.connection.setAutoCommit(false);
            int effectRows = 0;
            List<String> sqls = ListTs.newList();
            for (Map<String, Object> recordMap : record) {
                List<String> columns = new ArrayList<>();
                List<Object> objects = ListTs.newList();
                // determine
                for (String column : recordMap.keySet()) {
                    Object valuye = recordMap.get(column);
                    if (map != null) {
                        DatabaseColumnMetadata databaseColumnMetadata = opConfig.getMatchMapIgnoreCase(map, column);
                        if (databaseColumnMetadata == null) {
                            databaseColumnMetadata = opConfig.getMatchMapIgnoreCase(map, StrUtil.toUnderlineCase(column));
                            if (databaseColumnMetadata == null) {
                                continue;
                            }
                        }
                        Object o = recordMap.get(column);
                        if (skipUpdateNull && o == null) {
                            continue;
                        }
                        if (toUnderLine) {
                            column = StrUtil.toUnderlineCase(column);
                        }
                        column = escape(column);
                    }
                    objects.add(valuye);
                    columns.add(column);
                }
                String where = whereBuild.build(objects);
                Map<String, Object> params = Maps.newHashMap();
                params.put("TABLE_NAME", String.join(SP.DOT, ListTs.asNonNullList(schema, tableName)));
                params.put("COLUMNS_LIST", columns);
                params.put("COLUMNS", "(" + ListTs.join(",", columns) + ")");
                params.put("VALUES", ListTs.join(",", ListTs.map(columns, e -> columns + "=?")));
                params.put("VALUES_ZWF", ListTs.join(",", ListTs.map(columns, e -> "?")));
                params.put("WHERE", where);
                String sql = transferJdbcBatchUpdate(params);
                PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
                try {
                    Object[] array = objects.toArray();
                    StatementUtil.fillParams(preparedStatement, array);
                    effectRows += preparedStatement.executeUpdate();
                    Pair<String, Date> stringDatePair = recordSql(sql, connection, array);
                    if (stringDatePair != null) sqls.add(stringDatePair.getKey());
                } finally {
                    JdbcHelper.close(preparedStatement);
                }
            }
            long endTime = System.currentTimeMillis();
            if (isCommit) {
                this.connection.commit();
                this.connection.setAutoCommit(autoCommit);
            }
            psResult.setSql(sqls);
            psResult.setCostTime(endTime - beginTime);
            psResult.setEffectRows(effectRows);
        } catch (SQLException e) {
            if (isCommit) {
                try {
                    this.connection.rollback();
                } catch (SQLException ex) {
                    throw JdbcHelper.translateSqlException("jdbcInsert rollback", "", e);
                }
            }
            throw JdbcHelper.translateSqlException("jdbcInsert", "", e);
        }
        return psResult;
    }

    @Override
    public PsResult jdbcDelete(String tableName, String schema, boolean toUnderLine, boolean isCommit, WhereBuild whereBuild) {
        CheckUtils.notNull(tableName, "tableName");
        CheckUtils.notNull(whereBuild, "whereBuild");
        whereBuild.bind(connection);
        whereBuild.setToUnderLine(toUnderLine);
        schema = schema == null ? getConnectionSchema() : schema;
        tableName = escape(tableName);
        PsResult psResult = new PsResult();
        Map<String, DatabaseColumnMetadata> map = null;
        try {
            long beginTime = System.currentTimeMillis();
            boolean autoCommit = this.connection.getAutoCommit();
            if (isCommit) this.connection.setAutoCommit(false);
            int effectRows = 0;
            List<String> sqls = ListTs.newList();
            List<Object> objects = ListTs.newList();
            // determine
            String where = whereBuild.build(objects);
            Map<String, Object> params = Maps.newHashMap();
            params.put("TABLE_NAME", String.join(SP.DOT, ListTs.asNonNullList(schema, tableName)));
            params.put("WHERE", where);
            String sql = transferJdbcDelete(params);
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            try {
                Object[] array = objects.toArray();
                StatementUtil.fillParams(preparedStatement, array);
                effectRows += preparedStatement.executeUpdate();
                Pair<String, Date> stringDatePair = recordSql(sql, connection, array);
                if (null != stringDatePair) sqls.add(stringDatePair.getKey());
            } finally {
                JdbcHelper.close(preparedStatement);
            }
            long endTime = System.currentTimeMillis();
            if (isCommit) {
                this.connection.commit();
                this.connection.setAutoCommit(autoCommit);
            }
            psResult.setSql(sqls);
            psResult.setCostTime(endTime - beginTime);
            psResult.setEffectRows(effectRows);
        } catch (SQLException e) {
            if (isCommit) {
                try {
                    this.connection.rollback();
                } catch (SQLException ex) {
                    throw JdbcHelper.translateSqlException("jdbcInsert rollback", "", e);
                }
            }
            throw JdbcHelper.translateSqlException("jdbcInsert", "", e);
        }
        return psResult;
    }
}
