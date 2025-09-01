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
package easy4j.infra.dbaccess.dynamic.dll.op.impl.mp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.MetaInfoParse;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.*;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.List;

/**
 * ModelMetaInfoParse
 * 从模型中解析信息
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class ModelMetaInfoParse implements MetaInfoParse {

    @Getter
    OpContext opContext;

    @Setter
    DDLTableInfo ddlTableInfo;

    public ModelMetaInfoParse() {
    }

    public ModelMetaInfoParse(@NotNull DDLTableInfo ddlTableInfo, OpContext opContext) {
        this.opContext = opContext;
        this.ddlTableInfo = ddlTableInfo;
    }

    @Override
    public void setOpContext(OpContext opContext) {
        this.opContext = opContext;
    }

    @Override
    public DDLTableInfo parse() {
        try {
            return getDdlTableInfo();
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("DDLTableInfo Parse", "", e);
        }

    }

    private DDLTableInfo getDdlTableInfo() throws SQLException {
        CheckUtils.notNull(ddlTableInfo, "ddlTableInfo");
        CheckUtils.checkByLambda(this.ddlTableInfo, DDLTableInfo::getTableName);
        String tableName = this.ddlTableInfo.getTableName();
        String schema = this.ddlTableInfo.getSchema();
        //ddlTableInfo.setTableName(getTableName(aclass));
        IOpMeta opDbMeta = OpDbMeta.select(this.opContext.getConnection());
        List<DatabaseColumnMetadata> columns = opDbMeta.getColumns(this.opContext.getConnectionCatalog(), this.opContext.getConnectionSchema(), tableName);
        this.opContext.setDbColumns(columns);
        this.ddlTableInfo.setDbVersion(this.opContext.getDbVersion());
        this.ddlTableInfo.setSchema(this.opContext.getSchema());
        this.ddlTableInfo.setDbType(this.opContext.getDbType());

        List<TableMetadata> tableInfos1 = opDbMeta.getTableInfos(tableName);
        this.opContext.setTableMetadata(ListTs.get(tableInfos1, 0));

        List<PrimaryKeyMetadata> primaryKes = opDbMeta.getPrimaryKes(this.opContext.getConnectionCatalog(), this.opContext.getConnectionSchema(), tableName);
        this.opContext.setPrimaryKes(primaryKes);

        List<DDLFieldInfo> fieldInfoList = this.ddlTableInfo.getFieldInfoList();
        if (CollUtil.isNotEmpty(fieldInfoList)) {
            fieldInfoList.forEach(e -> {
                if (StrUtil.isBlank(e.getTableName())) {
                    e.setTableName(tableName);
                }
                if (StrUtil.isBlank(e.getSchema())) {
                    e.setSchema(e.getSchema());
                }
            });
        }

        return ddlTableInfo;
    }
}
