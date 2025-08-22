package easy4j.infra.dbaccess.dynamic.dll.op;
import easy4j.infra.dbaccess.dynamic.dll.op.api.*;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.util.function.Supplier;

public abstract class AbstractCombinationOp implements CombinationOp {

    public abstract OpDdlAlter getOpDdlAlter();
    public abstract OpDdlAlterAction getOpDdlAlterAction();
    public abstract OpDdlAlterRename getOpDdlAlterRename();
    public abstract OpSqlCommands getOpSqlCommands();
    public abstract OpDdlCreateTable getOpDdlCreateTable();

    public abstract OpContext getContext();

    public void init(){
        OpContext context = getContext();
        // alter 语句
        getOpDdlAlter().setOpContext(context);
        // alter [add | drop]  column
        getOpDdlAlterAction().setOpContext(context);
        // alter [rename]
        getOpDdlAlterRename().setOpContext(context);
        // 其他杂项比如说删除表格 删除视图等
        getOpSqlCommands().setOpContext(context);
        // create table | create table as
        getOpDdlCreateTable().setOpContext(context);
    }

    @Override
    public void addColumn() {

    }

    @Override
    public void dropColumn() {

    }

    @Override
    public void renameColumnName(String newColumnName) {

    }

    @Override
    public void renameConstraintName(String newConstraintName) {

    }

    @Override
    public void renameTableName(String newTableName) {

    }

    @Override
    public void setSchemaNewName(String schemaNewName) {

    }

    @Override
    public void setNewTableSpace(String newTableSpaceName) {

    }

    public <R> R callback(Supplier<R> consumer){
        try{
            return consumer.get();
        } finally {
            // clear resource
            OpContext context = this.getContext();
            Connection connection = context.getConnection();
            JdbcUtils.closeConnection(connection);
        }
    }
}
