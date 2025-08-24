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
package easy4j.infra.dbaccess.dynamic.dll.op.impl.tc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpTableConstraints;
import lombok.Getter;

import java.util.List;

/**
 * @author bokun.li
 * @date 2025/8/23
 */
@Getter
public abstract class AbstractOpTableConstraints implements OpTableConstraints {

    private OpContext opContext;

    @Override
    public void setOpContext(OpContext opContext) {
        this.opContext = opContext;
    }

    /**
     * CONSTRAINT [constraints_name] [PRIMARY KEY | UNIQUE | CHECK ]
     *
     * @param opContext
     * @return
     */
    @Override
    public List<String> getTableConstraints(OpContext opContext) {
        CheckUtils.checkByLambda(opContext, OpContext::getDdlTableInfo);
        List<String> segments = ListTs.newList();
        handlerConstraint(opContext.getDdlTableInfo(), opContext.getOpConfig(), segments);
        return segments;
    }

    private void handlerConstraint(DDLTableInfo ddlTableInfo, OpConfig opConfig, List<String> segments) {
        boolean hasExtraLine = false;
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        String tableName = ddlTableInfo.getTableName();
        if (CollUtil.isNotEmpty(fieldInfoList)) {
            List<DDLFieldInfo> primaryKey = ListTs.newList();
            List<DDLFieldInfo> uniqueKey = ListTs.newList();
            List<DDLFieldInfo> checkKey = ListTs.newList();
            List<DDLFieldInfo> constraintKey = ListTs.newList();
            for (DDLFieldInfo ddlFieldInfo : fieldInfoList) {
                String[] constraint = ddlFieldInfo.getConstraint();
                // primary key
                if (ddlFieldInfo.isPrimary()) {
                    primaryKey.add(ddlFieldInfo);
                }
                // unique
                if (ddlFieldInfo.isUnique()) {
                    uniqueKey.add(ddlFieldInfo);
                }
                // check
                if (StrUtil.isNotBlank(ddlFieldInfo.getCheck())) {
                    checkKey.add(ddlFieldInfo);
                }
                // custom constraint
                if (constraint != null && constraint.length > 0) {
                    constraintKey.add(ddlFieldInfo);
                }
            }
            int idx = 0;
            for (DDLFieldInfo ddlFieldInfo : primaryKey) {
                hasExtraLine = true;
                String name = ddlFieldInfo.getName();
                String columnName = opConfig.getColumnName(name);
                String tem = "CONSTRAINT pk_" + tableName + "_" + columnName + "_" + idx + " PRIMARY KEY (" + columnName + ")";
                segments.add(tem);
                idx++;
            }
            for (DDLFieldInfo ddlFieldInfo : uniqueKey) {
                hasExtraLine = true;
                String name = ddlFieldInfo.getName();
                String columnName = opConfig.getColumnName(name);
                String tem = "CONSTRAINT uk_" + tableName + "_" + columnName + "_" + idx + " UNIQUE (" + columnName + ")";
                segments.add(tem);
                idx++;
            }
            for (DDLFieldInfo ddlFieldInfo : checkKey) {
                hasExtraLine = true;
                String check = ddlFieldInfo.getCheck();
                String name = ddlFieldInfo.getName();
                String columnName = opConfig.getColumnName(name);
                String tem = "CONSTRAINT check_" + tableName + "_" + columnName + "_" + idx + " CHECK (" + check + ")";
                segments.add(tem);
                idx++;
            }
            for (DDLFieldInfo ddlFieldInfo : constraintKey) {
                hasExtraLine = true;
                String name = ddlFieldInfo.getName();
                String columnName = opConfig.getColumnName(name);
                String[] constraint = ddlFieldInfo.getConstraint();
                for (String s : constraint) {
                    String tem = "CONSTRAINT ctk_" + tableName + "_" + columnName + "_" + idx + SP.SPACE + s;
                    segments.add(tem);
                    idx++;
                }
            }
        }
        if (hasExtraLine) {
            String remove = segments.remove(segments.size() - 1);
            segments.add(StrUtil.replaceLast(remove, SP.COMMA, ""));
        }
    }

    @Override
    public List<String> getTableAttrs(OpContext opContext) {
        return null;
    }
}
