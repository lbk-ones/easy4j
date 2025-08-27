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
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpTableConstraints;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * @return
     */
    @Override
    public List<String> getTableConstraints() {
        CheckUtils.checkByLambda(opContext, OpContext::getDdlTableInfo);
        List<String> segments = ListTs.newList();
        handlerConstraint(opContext.getDdlTableInfo(), opContext.getOpConfig(), segments);
        return segments;
    }

    /**
     * fix 一个索引 多个字段的情况
     *
     * @param ddlTableInfo
     * @param opConfig
     * @param segments
     */
    private void handlerConstraint(DDLTableInfo ddlTableInfo, OpConfig opConfig, List<String> segments) {
        boolean hasExtraLine = false;
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        String tableName = ddlTableInfo.getTableName();
        if (CollUtil.isNotEmpty(fieldInfoList)) {
            List<DDLFieldInfo> primaryKey = ListTs.newList();
            List<DDLFieldInfo> uniqueKey = ListTs.newList();
            List<DDLFieldInfo> checkKey = ListTs.newList();
            List<DDLFieldInfo> constraintKey = ListTs.newList();
            // merge fieldInfoList
            Map<String, List<DDLFieldInfo>> fieldMergeMap = ListTs.groupBy(ListTs.filter(fieldInfoList, e ->
                    e.isPrimary() || e.isUnique() || StrUtil.isNotBlank(e.getCheck()) || ListTs.isNotEmpty(e.getConstraint())
            ), DDLFieldInfo::getIndexName);
            // 如果没有索引 那么 indexName 为 none
            for (String indeName : fieldMergeMap.keySet()) {
                List<DDLFieldInfo> _ddlFieldInfos = fieldMergeMap.get(indeName);
                List<DDLFieldInfo> ddlFieldInfos = null;

                List<String> columns = ListTs.mapToList(_ddlFieldInfos, DDLFieldInfo::getName);
                if("none".equals(indeName)){
                    ddlFieldInfos = _ddlFieldInfos;
                }else if(!columns.isEmpty()) {
                    ddlFieldInfos = ListTs.asList(_ddlFieldInfos.get(0));
                }
                assert ddlFieldInfos != null;
                ddlFieldInfos.sort(Comparator.comparingInt(DDLFieldInfo::getIndexSort));

                for (DDLFieldInfo ddlFieldInfo : ddlFieldInfos) {

                    // merge
                    if (columns.size() > 1 && !"none".equals(indeName)) {
                        // set a temp value
                        ddlFieldInfo.setTemp(String.join(SP.COMMA, columns));
                    }

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

            }

            int idx = 0;
            for (DDLFieldInfo ddlFieldInfo : primaryKey) {
                hasExtraLine = true;
                // first from temp value get
                String name = StrUtil.blankToDefault(ddlFieldInfo.getTemp(), ddlFieldInfo.getName());
                String columnName = opConfig.getColumnName(name);
                String unionName = opConfig.replaceSpecialSymbol(columnName);
                String cn = "pk_" + tableName + "_" + unionName + "_" + idx;
                cn = opConfig.get63UnderLineName(cn);
                String tem = "CONSTRAINT " + cn + " PRIMARY KEY (" + columnName + ")";
                segments.add(tem);
                idx++;
                ddlFieldInfo.setTemp(null);
            }
            for (DDLFieldInfo ddlFieldInfo : uniqueKey) {
                hasExtraLine = true;
                String name = StrUtil.blankToDefault(ddlFieldInfo.getTemp(), ddlFieldInfo.getName());
                String columnName = opConfig.getColumnName(name);
                String unionName = opConfig.replaceSpecialSymbol(columnName);
                String cn = "uk_" + tableName + "_" + unionName + "_" + idx;
                cn = opConfig.get63UnderLineName(cn);
                String tem = "CONSTRAINT " + cn + " UNIQUE (" + columnName + ")";
                segments.add(tem);
                idx++;
                ddlFieldInfo.setTemp(null);
            }
            for (DDLFieldInfo ddlFieldInfo : checkKey) {
                hasExtraLine = true;
                String check = ddlFieldInfo.getCheck();
                String name = ddlFieldInfo.getName();
                String columnName = opConfig.getColumnName(name);
                String unionName = opConfig.replaceSpecialSymbol(columnName);
                String cn = "check_" + tableName + "_" + unionName + "_" + idx;
                cn = opConfig.get63UnderLineName(cn);
                String tem = "CONSTRAINT " + cn + " CHECK (" + check + ")";
                segments.add(tem);
                idx++;
            }
            for (DDLFieldInfo ddlFieldInfo : constraintKey) {
                hasExtraLine = true;
                String name = ddlFieldInfo.getName();
                String columnName = opConfig.getColumnName(name);
                String unionName = opConfig.replaceSpecialSymbol(columnName);
                String[] constraint = ddlFieldInfo.getConstraint();
                String cn = "ctk_" + tableName + "_" + unionName + "_" + idx;
                cn = opConfig.get63UnderLineName(cn);
                for (String s : constraint) {
                    String tem = "CONSTRAINT" + cn + SP.SPACE + s;
                    segments.add(tem);
                    idx++;
                }
            }
        }
        if (hasExtraLine) {
            String remove = segments.remove(segments.size() - 1);
            if (remove.endsWith(SP.COMMA)) {
                segments.add(StrUtil.replaceLast(remove, SP.COMMA, ""));
            } else {
                segments.add(remove);
            }
        }
    }

    @Override
    public List<String> getTableOptions() {
        return null;
    }

    @Override
    public List<String> getPartitionOptions() {
        return null;
    }
}
