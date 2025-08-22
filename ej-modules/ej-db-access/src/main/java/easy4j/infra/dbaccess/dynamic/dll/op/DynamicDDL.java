package easy4j.infra.dbaccess.dynamic.dll.op;

import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.api.*;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.JavaClassMetaInfoParse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import javax.xml.validation.Schema;

@EqualsAndHashCode(callSuper = true)
@Data
public class DynamicDDL extends AbstractCombinationOp {

    private DataSource dataSource;

    private Schema schema;

    private Class<?> domainClass;

    private DDLTableInfo ddlTableInfo;

    private OpContext opContext;


    // parse from domainClass
    public DynamicDDL(@NotNull DataSource dataSource, Schema schema, @NotNull Class<?> domainClass) {
        CheckUtils.notNull(domainClass,"DynamicDDL domainClass");
        CheckUtils.notNull(dataSource,"DynamicDDL dataSource");
        this.dataSource = dataSource;
        this.schema = schema;
        this.domainClass = domainClass;
        super.init();
        JavaClassMetaInfoParse javaClassMetaInfoParse = new JavaClassMetaInfoParse();
        javaClassMetaInfoParse.setOpContext(this.getOpContext());
        this.ddlTableInfo = javaClassMetaInfoParse.parse();
        this.opContext.setDdlTableInfo(this.ddlTableInfo);
    }

    // parse from model
    public DynamicDDL(@NotNull DataSource dataSource, Schema schema, @NotNull DDLTableInfo ddlTableInfo) {
        CheckUtils.notNull(dataSource,"DynamicDDL dataSource");
        CheckUtils.notNull(ddlTableInfo,"DynamicDDL ddlTableInfo");
        CheckUtils.checkByLambda(ddlTableInfo,DDLTableInfo::getTableName);
        this.dataSource = dataSource;
        this.schema = schema;
        this.ddlTableInfo = ddlTableInfo;
        super.init();
    }

    // parse from dataSource
    public DynamicDDL(@NotNull DataSource dataSource,String tableName) {
        CheckUtils.notNull(dataSource,"DynamicDDL dataSource");
        CheckUtils.notNull(tableName,"DynamicDDL tableName");
        this.dataSource = dataSource;
        super.init();
    }

    OpContext initContext() {

        return null;
    }

    @Override
    public OpDdlAlter getOpDdlAlter() {
        return null;
    }

    @Override
    public OpDdlAlterAction getOpDdlAlterAction() {
        return null;
    }

    @Override
    public OpDdlAlterRename getOpDdlAlterRename() {
        return null;
    }

    @Override
    public OpSqlCommands getOpSqlCommands() {
        return null;
    }

    @Override
    public OpDdlCreateTable getOpDdlCreateTable() {
        return null;
    }


    @Override
    public OpContext getContext() {
        if (this.opContext == null) {
            OpContext opContext1 = new OpContext();
            this.opContext = opContext1;
        }
        return this.opContext;
    }
}
