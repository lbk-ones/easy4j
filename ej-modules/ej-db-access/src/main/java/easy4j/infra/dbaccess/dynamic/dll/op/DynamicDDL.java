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

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.api.*;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.mp.DataSourceMetaInfoParse;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.mp.JavaClassMetaInfoParse;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.mp.ModelMetaInfoParse;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.IOpMeta;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.SQLException;

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
    public DynamicDDL(String driverClassName,String url,String user,String password,String tableName) {
        this.dataSource = new TempDataSource(driverClassName,url,user,password);
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
            String catalog = connection.getCatalog();
            String schema1 = connection.getSchema();
            opContext.setConnectionCatalog(catalog);
            opContext.setConnectionSchema(schema1);
            Dialect dialect = JdbcHelper.getDialect(connection);
            // String dbType = InformationSchema.getDbType(dataSource, connection);
            IOpMeta opDbMeta = OpDbMeta.select(connection);
            String dbVersion = opDbMeta.getProductVersion();
            String dbType = opDbMeta.getDbType(connection);
            // String ddlTableName = getDDLTableName(dialect, aClass, getTableName(aClass, dialect));
            // 先取connection中的 schema 再取 catalog 这样可以兼容 mysql 、 postgresql 、 oracle 、sqlserver 的 其他的试过才知道，如果取错了 只能从外部传进来了
            if (StrUtil.isBlank(schema)) schema = StrUtil.blankToDefault(schema1, catalog);
            //List<DatabaseColumnMetadata> columns = opDbMeta.getColumns(catalog,schema,"");
            opContext.setDataSource(dataSource)
                    .setDdlTableInfo(this.ddlTableInfo)
                    .setConnection(connection)
                    .setSchema(schema)
                    .setOpConfig(opConfig == null ? new OpConfig() : opConfig)
                    //.setTableName(ddlTableName) 表名放到后续去处理
                    .setDbType(dbType)
                    .setDbVersion(dbVersion)
                    //.setDbColumns(columns)  放到后面去处理
                    .setDialect(dialect)
                    .setDomainClass(this.domainClass);

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
    public void close()  {
        OpContext context = getContext();
        Connection connection = context.getConnection();
        DataSourceUtils.releaseConnection(connection,this.getDataSource());
    }
}
