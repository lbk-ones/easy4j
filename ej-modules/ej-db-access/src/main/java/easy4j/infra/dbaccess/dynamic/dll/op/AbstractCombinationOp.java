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

import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.api.*;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.sc.CopyDbConfig;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * AbstractCombinationOp
 * 通用代理实现类
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public abstract class AbstractCombinationOp implements CombinationOp {

    public abstract OpDdlAlter getOpDdlAlter();

    public abstract OpSqlCommands getOpSqlCommands();

    public abstract OpDdlCreateTable getOpDdlCreateTable();

    public abstract OpContext getContext();

//    public void init() {
//        OpContext context = getContext();
//        // alter 语句
//        getOpDdlAlter().setOpContext(context);
//        // alter [add | drop]  column
//        getOpDdlAlterAction().setOpContext(context);
//        // alter [rename]
//        getOpDdlAlterRename().setOpContext(context);
//        // 其他杂项比如说删除表格 删除视图等
//        getOpSqlCommands().setOpContext(context);
//        // create table | create table as
//        getOpDdlCreateTable().setOpContext(context);
//    }

    @Override
    public void setOpContext(OpContext opContext) {
        // DO NOTING
    }


    @Override
    public String addColumn(DDLFieldInfo fieldInfo) {
        return callback(() ->
                getOpDdlAlter().addColumn(fieldInfo)
        );
    }

    @Override
    public String removeColumn(DDLFieldInfo fieldInfo) {
        return callback(() -> getOpDdlAlter().removeColumn(fieldInfo));
    }

    @Override
    public String renameColumnName(String oldName, String newColumnName) {
        return callback(() -> getOpDdlAlter().renameColumnName(oldName, newColumnName));
    }

    @Override
    public String renameConstraintName(String newConstraintName) {
        return callback(() -> getOpDdlAlter().renameConstraintName(newConstraintName));
    }

    @Override
    public String renameTableName(String newTableName) {
        return callback(() -> getOpDdlAlter().renameTableName(newTableName));

    }

    @Override
    public String setSchemaNewName(String schemaNewName) {
        return callback(() -> getOpDdlAlter().setSchemaNewName(schemaNewName));
    }

    @Override
    public String setNewTableSpace(String newTableSpaceName) {
        return callback(() -> getOpDdlAlter().setNewTableSpace(newTableSpaceName));
    }

    @Override
    public String getCreateTableDDL() {
        return callback(() -> getOpDdlCreateTable().getCreateTableDDL());
    }

    @Override
    public List<String> getCreateTableComments() {
        return callback(() -> getOpDdlCreateTable().getCreateTableComments());
    }

    @Override
    public void exeDDLStr(String segment) {
        getOpSqlCommands().exeDDLStr(segment);
    }

    @Override
    public void exeDDLStr(Connection connection, String segment, boolean isCloseConnection) {
        getOpSqlCommands().exeDDLStr(connection, segment, isCloseConnection);
    }

    @Override
    public Map<String, Object> dynamicSave(Map<String, Object> dict) {
        return getOpSqlCommands().dynamicSave(dict);
    }

    @Override
    public List<String> getIndexList() {
        return callback(() -> getOpDdlCreateTable().getIndexList());
    }

    @Override
    public String getFieldComment(DDLFieldInfo ddlFieldInfo) {
        return getOpDdlCreateTable().getFieldComment(ddlFieldInfo);
    }

    @Override
    public String autoDDLByJavaClass(boolean isExe) {
        return getOpSqlCommands().autoDDLByJavaClass(isExe);
    }

    @Override
    public List<String> copyDataSourceDDL(String[] tablePrefix, String[] tableType, CopyDbConfig copyDbConfig) {
        return getOpSqlCommands().copyDataSourceDDL(tablePrefix, tableType, copyDbConfig);
    }

    @Override
    public String dropTableIfExists(String tableName, boolean isExe) {
        return getOpDdlAlter().dropTableIfExists(tableName,isExe);
    }

    @Override
    public List<String> dropALlTableIfExists(boolean isExe) {
        return getOpDdlAlter().dropALlTableIfExists(isExe);

    }

    public <R> R callback(Supplier<R> consumer) {
        OpContext context = this.getContext();
        try {
            R r = consumer.get();
            OpConfig opConfig = context.getOpConfig();
            if (opConfig.isAutoExeDDL()) {
                if (r instanceof CharSequence) {
                    exeDDLStr(r.toString());
                }
            }
            return r;
        } finally {
            // clear resource
            //Connection connection = context.getConnection();
            //JdbcUtils.closeConnection(connection);
        }
    }
}
