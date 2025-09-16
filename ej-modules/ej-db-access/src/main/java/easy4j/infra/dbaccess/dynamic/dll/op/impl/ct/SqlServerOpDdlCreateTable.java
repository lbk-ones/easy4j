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
package easy4j.infra.dbaccess.dynamic.dll.op.impl.ct;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

import java.sql.Connection;
import java.util.List;

/**
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class SqlServerOpDdlCreateTable extends AbstractOpDdlCreateTable {

    @Override
    public boolean match(OpContext opContext) {
        return DbType.SQL_SERVER.getDb().equals(opContext.getDbType());
    }

    @Override
    public List<String> getCreateTableComments() {
        OpContext opContext = this.getOpContext();
        String tableComments = getTableComments();
        List<String> commentsList = ListTs.newList();
        if(StrUtil.isBlank(tableComments)) return commentsList;
        DDLTableInfo ddlTableInfo = opContext.getDdlTableInfo();
        String tableName = ddlTableInfo.getTableName();
        String schema = StrUtil.blankToDefault(ddlTableInfo.getSchema(), "dbo");
        String tableCc = "EXEC sp_addextendedproperty\n" +
                "@name = N'MS_Description'," +
                "@value = N'" + tableComments + "',\n" +
                "@level0type = N'SCHEMA',\n" +
                "@level0name = N'" + schema + "',\n" +
                "@level1type = N'TABLE',\n" +
                "@level1name = N'" + tableName + "'";
        commentsList.add(tableCc);
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        for (DDLFieldInfo ddlFieldInfo : fieldInfoList) {
            String fieldComment = this.getFieldComment(ddlFieldInfo);
            if (StrUtil.isNotBlank(fieldComment)) {
                commentsList.add(fieldComment);
            }
        }
        return commentsList;
    }

    @Override
    public String getFieldComment(DDLFieldInfo ddlFieldInfo) {

        // 这里不转义
        String fieldComments = getFieldComments(ddlFieldInfo);
        String schema = StrUtil.blankToDefault(ddlFieldInfo.getSchema(), "dbo");
        String tableName = ddlFieldInfo.getTableName();

        tableName = StrUtil.isBlankIfStr(tableName)?this.getOpContext().getTableName():tableName;
        schema = StrUtil.isBlankIfStr(schema)?this.getOpContext().getSchema():schema;

        String name = ddlFieldInfo.getName();
        return "EXEC sp_addextendedproperty \n" +
                "@name = N'MS_Description',\n" +
                "@value = N'" + fieldComments + "',\n" +
                "@level0type = N'SCHEMA',\n" +
                "@level0name = N'" + schema + "',\n" +
                "@level1type = N'TABLE',\n" +
                "@level1name = N'" + tableName + "',\n" +
                "@level2type = N'COLUMN',\n" +
                "@level2name = N'" + name + "'";
    }
}
