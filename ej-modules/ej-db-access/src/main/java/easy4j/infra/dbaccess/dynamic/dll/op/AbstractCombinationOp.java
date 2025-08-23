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
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
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

    public abstract OpDdlAlterAction getOpDdlAlterAction();

    public abstract OpDdlAlterRename getOpDdlAlterRename();

    public abstract OpSqlCommands getOpSqlCommands();

    public abstract OpDdlCreateTable getOpDdlCreateTable();

    public abstract OpContext getContext();

    public void init() {
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
    public void setOpContext(OpContext opContext) {
        // DO NOTING
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

    @Override
    public void addColumn(DDLFieldInfo fieldInfo) {

    }

    @Override
    public void removeColumn(DDLFieldInfo fieldInfo) {

    }

    @Override
    public void renameColumnName(DDLFieldInfo fieldInfo, String newColumnName) {

    }



    @Override
    public String getCreateTableDDL() {
        return getOpDdlCreateTable().getCreateTableDDL();
    }

    public <R> R callback(Supplier<R> consumer) {
        try {
            return consumer.get();
        } finally {
            // clear resource
            OpContext context = this.getContext();
            Connection connection = context.getConnection();
            JdbcUtils.closeConnection(connection);
        }
    }
}
