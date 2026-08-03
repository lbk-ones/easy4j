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
package easy4j.infra.dbaccess.dll.op;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.dialect.DialectFactory;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dll.op.api.OpDdlAlter;
import easy4j.infra.dbaccess.dll.op.api.OpDdlCreateTable;
import easy4j.infra.dbaccess.dll.op.api.OpSqlCommands;
import easy4j.infra.dbaccess.dll.op.impl.mp.DataSourceMetaInfoParse;
import easy4j.infra.dbaccess.dll.op.impl.mp.JavaClassMetaInfoParse;
import easy4j.infra.dbaccess.dll.op.impl.mp.ModelMetaInfoParse;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import easy4j.infra.dbaccess.orm.AccessConfig;
import easy4j.infra.dbaccess.orm.AccessUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;

import jakarta.validation.constraints.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * DynamicDDL
 * <p>
 * 功能函数聚合类（入口类）
 * <p>
 * 不能单例使用，每用一次new一次
 * <p>
 * 使用完close 或者
 * <p>
 * try (DynamicDDL sscElementTest = new DynamicDDL(xxx,xx,xx)) {
 * <p>
 * }
 * <p>
 *
 * @author bokun.li
 * @date 2025/8/23
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class DynamicDDL extends AbstractCombinationOp {

    private final DataSource dataSource;

    private String schema;

    private Class<?> domainClass;

    private DDLTableInfo ddlTableInfo;

    private OpContext opContext;

    @Setter
    private OpConfig opConfig;


    // parse from domainClass
    public DynamicDDL(@NotNull DataSource dataSource, String schema, @NotNull Class<?> domainClass) {
        CheckUtils.notNull(domainClass, "DynamicDDL domainClass");
        CheckUtils.notNull(dataSource, "DynamicDDL dataSource");
        this.dataSource = dataSource;
        this.schema = schema;
        this.domainClass = domainClass;
        this.ddlTableInfo = new JavaClassMetaInfoParse(this.getContext()).parse();
        this.opContext.setDdlTableInfo(this.ddlTableInfo);
        this.opContext.setTableName(this.ddlTableInfo.getTableName());
    }

    // parse from model
    public DynamicDDL(@NotNull DataSource dataSource, String schema, @NotNull DDLTableInfo ddlTableInfo) {
        CheckUtils.notNull(dataSource, "DynamicDDL dataSource");
        CheckUtils.notNull(ddlTableInfo, "DynamicDDL ddlTableInfo");
        CheckUtils.notNull(ddlTableInfo, "ddlTableInfo");
        this.dataSource = dataSource;
        this.schema = schema;
        this.ddlTableInfo = ddlTableInfo;
        this.ddlTableInfo = new ModelMetaInfoParse(ddlTableInfo, this.getContext()).parse();
        this.opContext.setDdlTableInfo(ddlTableInfo);
        this.opContext.setTableName(this.ddlTableInfo.getTableName());
    }

    // parse from dataSource
    public DynamicDDL(@NotNull DataSource dataSource, String tableName) {
        CheckUtils.notNull(dataSource, "DynamicDDL dataSource");
        CheckUtils.notNull(tableName, "DynamicDDL tableName");
        this.dataSource = dataSource;
        this.ddlTableInfo = new DataSourceMetaInfoParse(this.dataSource, tableName, this.getContext()).parse();
        this.opContext.setDdlTableInfo(ddlTableInfo);
        this.opContext.setTableName(this.ddlTableInfo.getTableName());
    }

    // only parse DataSource
    public DynamicDDL(@NotNull DataSource dataSource) {
        CheckUtils.notNull(dataSource, "DynamicDDL dataSource");
        this.dataSource = dataSource;
        this.ddlTableInfo = null;
        getContext();
    }

    // parse from other db connection info
    public DynamicDDL(String driverClassName, String url, String user, String password, String tableName) {
        this.dataSource = new TempDataSource(driverClassName, url, user, password);
        this.ddlTableInfo = null;
        OpContext context = getContext();
        context.setTableName(tableName);
    }

    @Override
    public OpDdlAlter getOpDdlAlter() {
        return OpSelector.selectOpDdlAlter(this.getContext());
    }

    @Override
    public OpSqlCommands getOpSqlCommands() {
        return OpSelector.selectOpSqlCommands(getContext());
    }

    @Override
    public OpDdlCreateTable getOpDdlCreateTable() {
        return OpSelector.selectOpCreateTable(this.getContext());
    }


    @Override
    public OpContext getContext() {
        if (this.opContext == null) {
            this.opContext = initContext();
        }
        return this.opContext;
    }

    public OpContext initContext() {
        Connection connection = null;
        String ddl = null;
        boolean hasException = false;
        try {
            OpContext opContext = new OpContext();
            connection = DataSourceUtils.getConnection(this.getDataSource());
            String catalog = StrUtil.trim(connection.getCatalog());
            String schema1 = StrUtil.trim(connection.getSchema());
            opContext.setConnectionCatalog(catalog);
            opContext.setConnectionSchema(schema1);
            Dialect dialect = DialectFactory.get(connection);
            // String dbType = InformationSchema.getDbType(dataSource, connection);
            String dbVersion = dialect.getProductVersion();
            String dbType = dialect.getDbType();
            // String ddlTableName = getDDLTableName(dialect, aClass, getTableName(aClass, dialect));
            // 先取connection中的 schema 再取 catalog 这样可以兼容 mysql 、 postgresql 、 oracle 、sqlserver 的 其他的试过才知道，如果取错了 只能从外部传进来了
            if (StrUtil.isBlank(schema)) schema = StrUtil.blankToDefault(schema1, catalog);
            OpConfig opConfig1 = opConfig == null ? new OpConfig() : opConfig;
            //List<DatabaseColumnMetadata> columns = opDbMeta.getColumns(catalog,schema,"");

            AccessConfig accessConfig = new AccessConfig();
            accessConfig.setDataSource(dataSource);
            AccessUtils accessUtils = new AccessUtils(accessConfig);
            opContext.setDataSource(dataSource)
                    .setDdlTableInfo(this.ddlTableInfo)
                    .setConnection(connection)
                    .setSchema(schema)
                    .setOpConfig(opConfig1)
                    .setDbType(dbType)
                    .setDbVersion(dbVersion)
                    .setDialect(dialect)
                    .setAccessUtils(accessUtils)
                    .setDomainClass(this.domainClass);
            if(this.domainClass!=null){
                opConfig1.setPgAutoLowerCase(true);
                opConfig1.setH2AutoUpperCase(true);
                opConfig1.setOracleAutoUpperCase(true);
                opConfig1.setDb2AutoUpperCase(true);
                if(Objects.equals(dbType, DbType.POSTGRE_SQL.getDb())){
                    opConfig1.setToLowCase(true);
                }
                if(Objects.equals(dbType, DbType.H2.getDb())){
                    opConfig1.setToUpperCase(true);
                }
                if(Objects.equals(dbType, DbType.ORACLE.getDb())){
                    opConfig1.setToUpperCase(true);
                }
                if(Objects.equals(dbType, DbType.DB2.getDb())){
                    opConfig1.setToUpperCase(true);
                }
            }
            return opContext;
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

    @Override
    public void close() {
        OpContext context = getContext();
        Connection connection = context.getConnection();
        DataSourceUtils.releaseConnection(connection, this.getDataSource());
    }
}
