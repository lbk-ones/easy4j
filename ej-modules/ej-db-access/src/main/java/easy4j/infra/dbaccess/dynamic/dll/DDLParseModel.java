package easy4j.infra.dbaccess.dynamic.dll;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.dynamic.dll.ad.AnsiAdFieldStrategy;
import easy4j.infra.dbaccess.dynamic.dll.ct.DDLParseExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.DdlCtModelExecutor;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.InformationSchema;
import easy4j.infra.dbaccess.helper.DDlHelper;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 动态ddl解析，从domain模型然后逆向到数据库
 *
 * @author bokun.li
 * @date 2025-08-03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DDLParseModel extends CommonDBAccess implements DDLParse {

    private String schema;

    private final DataSource dataSource;

    private boolean enableExe;

    private boolean isToUnderLine = true;

    private final DDLTableInfo ddlTableInfo;

    private DDLConfig dllConfig;

    /**
     * @param ddlTableInfo 传入表模型
     * @param dataSource   传入数据源
     * @param schema       传入schema
     */
    public DDLParseModel(DDLTableInfo ddlTableInfo, DataSource dataSource, String schema) {
        CheckUtils.notNull(ddlTableInfo, "ddlTableInfo");
        CheckUtils.notNull(ddlTableInfo, "tableName");
        CheckUtils.checkByPath(ddlTableInfo, "fieldInfoList");
        CheckUtils.notNull(dataSource, "dataSource");
        this.ddlTableInfo = ddlTableInfo;
        this.dataSource = dataSource;
        this.schema = schema;
        mount();
    }

    public void mount() {
        Connection connection = null;
        String ddl = null;
        boolean hasException = false;
        try {
            this.dllConfig = new DDLConfig();
            connection = dataSource.getConnection();
            String catalog = connection.getCatalog();
            String schema1 = connection.getSchema();
            this.dllConfig.setConnectionCatalog(catalog);
            this.dllConfig.setConnectionSchema(schema1);
            String dbType = InformationSchema.getDbType(dataSource, connection);
            String dbVersion = InformationSchema.getDbVersion(dataSource, connection);
            String ddlTableName = this.ddlTableInfo.getTableName();
            List<DDLFieldInfo> fieldInfoList = this.ddlTableInfo.getFieldInfoList();
            fieldInfoList.forEach(ddlFieldInfo -> {
                ddlFieldInfo.setDbType(dbType);
                ddlFieldInfo.setDbVersion(dbVersion);
            });
            this.ddlTableInfo.setDbType(dbType);
            this.ddlTableInfo.setDbVersion(dbVersion);

            if (StrUtil.isBlank(schema)) schema = StrUtil.blankToDefault(schema1, catalog);

            List<DynamicColumn> columns = InformationSchema.getColumns(dataSource, schema, ddlTableName, connection);


            this.dllConfig.setDataSource(dataSource)
                    .setConnection(connection)
                    .setSchema(schema)
                    .setTableName(ddlTableName)
                    .setDbType(dbType)
                    .setDbVersion(dbVersion)
                    .setDbColumns(columns)
                    .setDialect(JdbcHelper.getDialect(connection))
                    .setDdlTableInfo(this.ddlTableInfo);

        } catch (SQLException sqlE) {
            hasException = true;
            throw JdbcHelper.translateSqlException("", ddl, sqlE);
        } catch (Exception e) {
            hasException = true;
            throw e;
        } finally {
            if (hasException) {
                JdbcHelper.close(connection);
            }
        }
    }

    // 创建表sql
    public String getCreateTableTxt() {

        DDLParseExecutor ddlCtExecutor = new DdlCtModelExecutor(this.dllConfig);
        return ddlCtExecutor.execute();
    }

    // 新增字段sql
    public String getAddColumnTxt() {

        List<DDLFieldInfo> fieldInfoList = this.ddlTableInfo.getFieldInfoList();
        List<DynamicColumn> dbColumns = this.dllConfig.getDbColumns();
        Map<String, DynamicColumn> dbColumnsMap = ListTs.mapOne(dbColumns, DynamicColumn::getColumnName);
        List<DDLFieldInfo> newAdList = ListTs.newList();
        for (DDLFieldInfo field : fieldInfoList) {
            String name = field.getName();
            String columnName = this.dllConfig.getColumnName(name);
            DynamicColumn orDefault = dbColumnsMap.get(columnName);
            // only not exist can new add
            if (Objects.isNull(orDefault)) {
                newAdList.add(field);
            }
        }
        if (CollUtil.isNotEmpty(newAdList)) {
            this.dllConfig.setAdColumns(newAdList);
            AnsiAdFieldStrategy ansiAdFieldStrategy = new AnsiAdFieldStrategy();
            String columnSegment = ansiAdFieldStrategy.getColumnSegment(this.dllConfig);
            String columnComment = ansiAdFieldStrategy.getColumnComment(this.dllConfig);
            return ListTs.asList(columnSegment, columnComment)
                    .stream()
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE));
        }
        return "";
    }

    public String getDDLTxt() {

        if (CollUtil.isNotEmpty(this.dllConfig.getDbColumns())) {
            return getAddColumnTxt();
        } else {
            return getCreateTableTxt();
        }
    }

    public void execDDL() {
        try {
            String ddl = getDDLTxt();
            DDlHelper.execDDL(this.dllConfig.getConnection(), ddl, null, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDDLFragment() {
        return getDDLTxt();
    }


}
