package easy4j.infra.dbaccess.dll.op.impl.sc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dialect.DialectFactory;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dll.op.OpConfig;
import easy4j.infra.dbaccess.dll.op.OpContext;
import easy4j.infra.dbaccess.dll.op.OpSelector;
import easy4j.infra.dbaccess.dll.op.api.OpDdlAlter;
import easy4j.infra.dbaccess.dll.op.api.OpDdlCreateTable;
import easy4j.infra.dbaccess.dll.op.api.OpSqlCommands;
import easy4j.infra.dbaccess.dll.op.impl.mp.DataSourceMetaInfoParse;
import easy4j.infra.dbaccess.dll.op.meta.DatabaseColumnMetadata;
import easy4j.infra.dbaccess.dll.op.meta.TableMetadata;

import easy4j.infra.dbaccess.helper.DDlHelper;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractOpSqlCommands implements OpSqlCommands {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    OpContext opContext;

    @Override
    public boolean match(OpContext opContext) {
        return true;
    }

    @Override
    public void setOpContext(OpContext opContext) {
        this.opContext = opContext;
    }

    @Override
    public void exeDDLStr(String segment) {
        String ddl = StrUtil.trim(segment);
        if (StrUtil.isBlank(segment)) return;
        ddl = StrUtil.addSuffixIfNot(ddl, SP.SEMICOLON);
        if (StrUtil.isNotBlank(ddl)) {
            try {
                DDlHelper.execDDL(getOpContext().getConnection(), ddl, null, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<DatabaseColumnMetadata> checkDynamic(Map<String, Object> dict) {
        OpContext opContext1 = this.getOpContext();
        // table is if exist？
        TableMetadata tableMetadata = opContext1.getTableMetadata();
        if (null == tableMetadata) {
            throw EasyException.wrap(BusCode.A00060, opContext1.getTableName());
        }
        // only table can insert or update!
        String tableType = tableMetadata.getTableType();
        if (!StrUtil.equalsIgnoreCase(tableType, "TABLE")) {
            throw EasyException.wrap(BusCode.A00047, tableType);
        }
        List<DatabaseColumnMetadata> dbColumns = opContext1.getDbColumns();
        CheckUtils.notNull(dbColumns, "dbColumns");

        String tableName = opContext1.getTableName();
        CheckUtils.notNull(tableName, "tableName");

        return dbColumns;
    }

    @Override
    public void exeDDLStr(Connection newConnection, String segment, boolean isCloseConnection) {
        if (newConnection == null) return;
        String ddl = StrUtil.trim(segment);
        if (StrUtil.isBlank(segment)) return;
        ddl = StrUtil.addSuffixIfNot(ddl, SP.SEMICOLON);
        if (StrUtil.isNotBlank(ddl)) {
            try {
                DDlHelper.execDDL(newConnection, ddl, null, isCloseConnection);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void prepare(Map<String, Object> dict, List<String> dbColumnNmes, List<Object> objects, List<String> fieldNames, List<String> zwf) {
        Set<String> keys = dict.keySet();
        for (String key : keys) {
            String key2 = StrUtil.toUnderlineCase(key);
            if (!dbColumnNmes.contains(key2)) {
                if (!dbColumnNmes.contains(key2.toUpperCase())) {
                    if (!dbColumnNmes.contains(key2.toLowerCase())) {
                        continue;
                    }
                }
            }
            Object o = dict.get(key);
            if (o == null) {
                o = dict.get(key2);
                if (o == null) {
                    o = dict.get(key2.toUpperCase());
                    if (o == null) {
                        o = dict.get(key2.toLowerCase());
                    }
                }
            }

            objects.add(o);
            fieldNames.add(key2);
            zwf.add(SP.QUESTION_MARK);
        }
    }

    /**
     * 通过 java Class对象 自动执行ddl语句 没有就建表，有就检测要新增得字段，只新增不修改
     *
     * @return
     */
    @Override
    public String autoDDLByJavaClass(boolean isExe) {
        OpContext opContext1 = this.getOpContext();
        // the parameter domainClass must be passed into the dynamic ddl
        CheckUtils.checkByPath(opContext1, "domainClass", "ddlTableInfo");
        DDLTableInfo ddlTableInfo = opContext1.getDdlTableInfo();
        CheckUtils.notNull(ddlTableInfo, "ddlTableInfo");
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        if (CollUtil.isEmpty(fieldInfoList)) return "";
        OpConfig opConfig = opContext1.getOpConfig();
        CheckUtils.notNull(opConfig, "opConfig");
        List<DatabaseColumnMetadata> dbColumns = opContext1.getDbColumns();
        String sqlSegment = null;
        OpDdlCreateTable opDdlCreateTable = OpSelector.selectOpCreateTable(opContext1);
        if (CollUtil.isNotEmpty(dbColumns)) {
            // pick need add columns
            OpDdlAlter opDdlAlter = OpSelector.selectOpDdlAlter(opContext1);
            List<DDLFieldInfo> collect = fieldInfoList.stream().filter(e -> dbColumns
                    .stream()
                    .noneMatch(e2 -> {
                        String name = e.getName();
                        String columnName = opConfig.getColumnName(name);
                        return StrUtil.equalsIgnoreCase(e2.getColumnName(), columnName);
                    })).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(collect)) {
                List<String> map = ListTs.map(collect, opDdlAlter::addColumn);
                List<String> map2 = ListTs.map(collect, opDdlCreateTable::getFieldComment).stream().filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(map2)) map.addAll(map2);
                sqlSegment = String.join(SP.SEMICOLON + SP.NEWLINE, map);
            }
        } else {
            String createTableDDL = opDdlCreateTable.getCreateTableDDL();
            List<String> createTableComments = opDdlCreateTable.getCreateTableComments();
            List<String> createTableIndexList = opDdlCreateTable.getIndexList();
            sqlSegment = ListTs.asList(
                            StrUtil.removeSuffix(createTableDDL, SP.SEMICOLON),
                            ListTs.join(SP.SEMICOLON + SP.NEWLINE, createTableComments),
                            ListTs.join(SP.SEMICOLON + SP.NEWLINE, createTableIndexList))
                    .stream()
                    .filter(ObjectUtil::isNotEmpty)
                    .collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE));
        }
        sqlSegment = StrUtil.addSuffixIfNot(sqlSegment, SP.SEMICOLON);
        // exe
        if (isExe) exeDDLStr(sqlSegment);
        return sqlSegment;
    }

    /**
     * 改变上下文
     *
     * @param copyDbConfig
     */
    public boolean changeContext(CopyDbConfig copyDbConfig) {
        String ToDbType;
        Connection toConnection = null;
        Dialect opDbMeta = null;
        DataSource toDataSource = null;
        String catalog = null;
        String schema1 = null;
        if (copyDbConfig != null) {
            CheckUtils.checkByLambda(copyDbConfig, CopyDbConfig::getDataSource);
            toDataSource = copyDbConfig.getDataSource();
            if (toDataSource == this.opContext.getDataSource()) {
                return false;
            }
            try {
                toConnection = toDataSource.getConnection();
                ToDbType = JdbcHelper.getDatabaseType(toConnection);
                if (StrUtil.isBlank(ToDbType)) {
                    throw new IllegalArgumentException(" !! not support database " + copyDbConfig);
                }
                opDbMeta = DialectFactory.get(toConnection);
                catalog = toConnection.getCatalog();
                schema1 = toConnection.getSchema();

            } catch (SQLException e) {
                throw JdbcHelper.translateSqlException("copyDataSourceDDL get source connection", null, e);
            }
        }
        assert opDbMeta != null;
        String dbType = opDbMeta.getDbType();
        String productVersion = opDbMeta.getProductVersion();
        this.opContext.setDbType(dbType);
        this.opContext.setDbVersion(productVersion);
        this.opContext.setSchema(StrUtil.blankToDefault(schema1, catalog));
        this.opContext.setConnectionCatalog(catalog);
        this.opContext.setConnectionSchema(schema1);
        this.opContext.setDialect(opDbMeta);
        this.opContext.setDataSource(toDataSource);
        // 反写回去
        copyDbConfig.setConnection(toConnection);
        this.opContext.setConnection(toConnection);
        return true;
    }

    /**
     * 数据库存储时候自动转换
     * 比如oracle、h2、db2 会自动转成大写等
     * mysql默认大小写不敏感，如果mysql开启了全部小写那么就敏感了 所以这里很难去抉择
     * sqlserver的大小写也不敏感
     * 如果返回 unknown 那么代表大小写不敏感 TestTable 和 TestTABLE 是两个不同的表
     * 如果返回 upper或者lower那么代表 TestTable 和 TestTABLE 从逻辑上说是一个表
     *
     * @param dbType
     * @return
     */
    public String getTableNameUpperOrLower(String dbType) {
        String caseStr = "";
        if (StrUtil.equals(dbType, DbType.ORACLE.getDb())) {
            caseStr = "upper";
        } else if (StrUtil.equals(dbType, DbType.MYSQL.getDb())) {
            caseStr = "unknown";
        } else if (StrUtil.equals(dbType, DbType.H2.getDb())) {
            caseStr = "upper";
        } else if (StrUtil.equals(dbType, DbType.DB2.getDb())) {
            caseStr = "upper";
        } else if (StrUtil.equals(dbType, DbType.POSTGRE_SQL.getDb())) {
            caseStr = "lower";
        } else if (StrUtil.equals(dbType, DbType.SQL_SERVER.getDb())) {
            // sqlserver会统一大小写之后在检查是否存在所以它也是区分大小写的
            caseStr = "unknown2";
        }
        return caseStr;
    }

    @Override
    public List<String> copyDataSourceDDL(String[] tablePrefix, String[] tableType, CopyDbConfig copyDbConfig) {
        CheckUtils.checkByLambda(this.opContext, OpContext::getDataSource, OpContext::getConnection);
        if (ListTs.isEmpty(tableType)) tableType = new String[]{"TABLE"};
        Connection oldConnection = this.opContext.getConnection();
        Dialect select = DialectFactory.get(oldConnection);
        List<TableMetadata> allTableList = ListTs.newList();
        // get all or custom tableInfo
        if (ListTs.isEmpty(tablePrefix)) {
            List<TableMetadata> allTableInfo = select.getAllTableInfoByTableType(null, tableType);
            ListTs.addAll(allTableList, allTableInfo);
        } else {
            for (String prefix : tablePrefix) {
                List<TableMetadata> allTableInfo = select.getAllTableInfoByTableType(prefix, tableType);
                ListTs.addAll(allTableList, allTableInfo);
            }
        }
        DataSource oldDataSource = this.opContext.getDataSource();
        String oldDbType = this.opContext.getDbType();
        String oldDbVersion = this.opContext.getDbVersion();
        String oldSchema = this.opContext.getSchema();
        String oldConnectionCatalog = this.opContext.getConnectionCatalog();
        String oldConnectionSchema = this.opContext.getConnectionSchema();
        DDLTableInfo oldDdlTableInfo = this.opContext.getDdlTableInfo();
        String oldTableName = this.opContext.getTableName();
        TableMetadata oldTableMetadata = this.opContext.getTableMetadata();
        List<String> joinRes = ListTs.newList();
        boolean isChangeContext = false;
        boolean isExe = false;
        try {
            // load target dataSource all table
            Map<String, TableMetadata> map = Maps.newHashMap();
            String copyTargetDbType = null;
            if (null != copyDbConfig) {
                CheckUtils.checkByLambda(copyDbConfig, CopyDbConfig::getDataSource);
                DataSource dataSource = copyDbConfig.getDataSource();
                try (Connection connection = dataSource.getConnection()) {
                    Dialect select1 = DialectFactory.get(connection);
                    copyTargetDbType = select.getDbType();
                    List<TableMetadata> allTableInfoByTableType = select1.getAllTableInfoByTableType(null, new String[]{"TABLE"});
                    map = ListTs.toMap(allTableInfoByTableType, TableMetadata::getTableName);
                } catch (SQLException e) {
                    throw JdbcHelper.translateSqlException("newDataSourceGetConnection", null, e);
                }
            }
            List<List<String>> objects = ListTs.newList();
            List<DDLTableInfo> ddlTableInfos = ListTs.newList();
            OpConfig opConfig = opContext.getOpConfig();
            Set<String> distinct = new HashSet<>();
            // parse to DDLTableInfo
            for (TableMetadata tableMetadata : allTableList) {
                String tableName = tableMetadata.getTableName();
                String lowerCase = tableName.toLowerCase();
                String tableNameUpperOrLower = getTableNameUpperOrLower(copyTargetDbType);
                if (distinct.contains(lowerCase) && !StrUtil.equals("unknown", tableNameUpperOrLower)) {
                    log.info("table name repeat so skip the table " + tableName);
                    continue;
                } else {
                    distinct.add(lowerCase);
                }
                TableMetadata matchMapIgnoreCase = opConfig.getMatchMapIgnoreCase(map, tableName);
                if (null != matchMapIgnoreCase) {
                    log.info("skip table " + tableName);
                    continue;
                }
                DataSourceMetaInfoParse dataSourceMetaInfoParse = new DataSourceMetaInfoParse(oldDataSource, tableMetadata.getTableName(), this.opContext);
                dataSourceMetaInfoParse.setCopyTargetDbType(copyTargetDbType);
                dataSourceMetaInfoParse.setEscapeTableName(copyDbConfig != null && copyDbConfig.isEscapeTableName());

                DDLTableInfo parse = dataSourceMetaInfoParse.parse();
                List<DDLFieldInfo> fieldInfoList = parse.getFieldInfoList();
                if (CollUtil.isEmpty(fieldInfoList)) {
                    log.info("the table's fields is empty " + tableMetadata.getTableName());
                    continue;
                }
                parse.setTableMetadata(tableMetadata);
                ddlTableInfos.add(parse);
            }
            String newTablePrefix = null;
            String newTableSuffix = null;
            // prepare transfer db context
            if (null != copyDbConfig) {
                newTablePrefix = copyDbConfig.getTablePrefix();
                newTableSuffix = copyDbConfig.getTableSuffix();
                isChangeContext = changeContext(copyDbConfig);
                isExe = copyDbConfig.isExe();
            }
            for (DDLTableInfo ddlTableInfo : ddlTableInfos) {
                String tableName = ddlTableInfo.getTableName();
                TableMetadata tableMetadata = ddlTableInfo.getTableMetadata();
                if (isChangeContext) {
                    String schema = this.opContext.getSchema();
                    String dbType = this.opContext.getDbType();
                    String dbVersion = this.opContext.getDbVersion();
                    if (StrUtil.isNotBlank(newTablePrefix)) tableName = newTablePrefix += tableName;
                    if (StrUtil.isNotBlank(newTableSuffix)) tableName = tableName + newTableSuffix;
                    List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
                    String finalTableName = tableName;
                    ListTs.foreach(fieldInfoList, e -> {
                        e.setSource("1");
                        e.setTableName(finalTableName);
                        e.setSchema(schema);
                        e.setDbType(dbType);
                        e.setDbVersion(dbVersion);
                    });
                    List<DDLIndexInfo> ddlIndexInfoList = ddlTableInfo.getDdlIndexInfoList();
                    ListTs.foreach(ddlIndexInfoList, e -> {
                        e.setSchema(schema);
                        e.setTableName(finalTableName);
                    });

                    tableMetadata.setTableName(finalTableName);
                    tableMetadata.setTableSchem(schema);
                    ddlTableInfo.setTableName(finalTableName);
                    ddlTableInfo.setSchema(schema);
                    ddlTableInfo.setDbType(dbType);
                    ddlTableInfo.setDbVersion(dbVersion);
                }
                // force enable if not exists
                ddlTableInfo.setIfNotExists(true);
                this.opContext.setDdlTableInfo(ddlTableInfo);
                this.opContext.setTableName(tableName);
                this.opContext.setTableMetadata(tableMetadata);
                OpDdlCreateTable opDdlCreateTable = OpSelector.selectOpCreateTable(this.opContext);
                List<String> res = ListTs.newList();
                // create table
                String createTableDDL = opDdlCreateTable.getCreateTableDDL();
                ListTs.add(res, createTableDDL);
                // comments
                List<String> createTableComments = opDdlCreateTable.getCreateTableComments();
                ListTs.addAll(res, createTableComments);
                // index
                List<String> indexList = opDdlCreateTable.getIndexList();
                ListTs.addAll(res, indexList);
                objects.add(res);
            }
            for (List<String> object : objects) {
                List<String> collect = object.stream().map(e -> StrUtil.addSuffixIfNot(e, SP.SEMICOLON)).collect(Collectors.toList());
                String join = ListTs.join("\n", collect);
                join = StrUtil.addSuffixIfNot(join, SP.SEMICOLON);
                joinRes.add(join);
            }
        } finally {
            if (isChangeContext) {
                Connection newConnection = this.opContext.getConnection();
                if (isExe && ListTs.isNotEmpty(joinRes)) {
                    exeDDLStr(newConnection, ListTs.join("\n", joinRes), true);
                } else {
                    // Directly close third-party connections
                    JdbcHelper.close(newConnection);
                }
                // Put the original newConnection back in place for external calls to automatically close
                this.opContext.setDataSource(oldDataSource);
                this.opContext.setConnection(oldConnection);
                this.opContext.setDbType(oldDbType);
                this.opContext.setDbVersion(oldDbVersion);
                this.opContext.setSchema(oldSchema);
                this.opContext.setConnectionCatalog(oldConnectionCatalog);
                this.opContext.setConnectionSchema(oldConnectionSchema);
                this.opContext.setDataSource(oldDataSource);
                this.opContext.setConnection(oldConnection);
                this.opContext.setDdlTableInfo(oldDdlTableInfo);
                this.opContext.setTableName(oldTableName);
                this.opContext.setTableMetadata(oldTableMetadata);
            }
        }

        return joinRes;
    }

    @Override
    public boolean theColumnIsExists(DDLFieldInfo ddlFieldInfo) {
        OpContext opContext1 = this.getOpContext();
        CheckUtils.checkByLambda(opContext1, OpContext::getConnectionCatalog, OpContext::getConnectionSchema, OpContext::getTableName);
        Connection connection = opContext1.getConnection();
        Dialect select = DialectFactory.get(connection);
        List<DatabaseColumnMetadata> dbColumns;
        try {
            dbColumns = select.getColumnsNoCache(opContext1.getConnectionCatalog(), opContext1.getConnectionSchema(), opContext1.getTableName());
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("theColumnIsExists", null, e);
        }
        if (ListTs.isEmpty(dbColumns)) {
            log.error("need in construct input table name! or the tableName need has var：" + opContext1.getTableName());
            return false;
        }
        String name = ddlFieldInfo.getName();
        return dbColumns.stream().anyMatch(e -> StrUtil.equalsIgnoreCase(e.getColumnName(), name));
    }
}
