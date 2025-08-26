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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.OpSelector;
import easy4j.infra.dbaccess.dynamic.dll.op.api.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author bokun.li
 * @date 2025/8/23
 */
@Getter
public abstract class AbstractOpDdlCreateTable implements OpDdlCreateTable {

    private OpContext opContext;

    // 保存参数名称的map
    private static final Map<String, String> FIELD_MAP = Maps.newHashMap();
    // 额外的参数 这个会覆盖 getTemplateParams 方法获取的参数
    private final Map<String, String> extParamMap = Maps.newHashMap();

    public static final String CREATE_DEFINITION = "CREATE_DEFINITION";
    public static final String TABLE_OPTIONS = "TABLE_OPTIONS";
    public static final String PARTITION_OPTIONS = "PARTITION_OPTIONS";
    public static final String TEMPORARY = "TEMPORARY";
    public static final String IF_NOT_EXIST = "IF_NOT_EXIST";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String UN_LOGGED = "UN_LOGGED";

    static {
        FIELD_MAP.put(CREATE_DEFINITION, CREATE_DEFINITION);
        FIELD_MAP.put(TABLE_OPTIONS, TABLE_OPTIONS);
        FIELD_MAP.put(PARTITION_OPTIONS, PARTITION_OPTIONS);
        FIELD_MAP.put(TEMPORARY, TEMPORARY);
        FIELD_MAP.put(IF_NOT_EXIST, IF_NOT_EXIST);
        FIELD_MAP.put(TABLE_NAME, TABLE_NAME);
        FIELD_MAP.put(UN_LOGGED, UN_LOGGED);
    }


    public String getTemplate() {

        return "create [" + TEMPORARY + "] [" + UN_LOGGED + "] table [" + IF_NOT_EXIST + "] [" + TABLE_NAME + "](\n" +
                "   [" + CREATE_DEFINITION + "]\n" +
                ")\n" +
                "[" + TABLE_OPTIONS + "]\n" +
                "[" + PARTITION_OPTIONS + "]";
    }

    /**
     * 如果模板参数不是默认的那些参数名称，那么子类就要调用这个方法给模板参数传值
     *
     * @author bokun.li
     * @date 2025-08-24
     */
    public void put(String field, String value) {
        FIELD_MAP.putIfAbsent(field, field);
        extParamMap.putIfAbsent(field, value);
    }

    @Override
    public void setOpContext(OpContext opContext) {
        if (this.opContext == null) {
            this.opContext = opContext;
        }
    }

    public Map<String, String> getTemplateParams(DDLTableInfo ddlTableInfo) {
        Map<@Nullable String, @Nullable String> res = Maps.newHashMap();
        List<DDLIndexInfo> ddlIndexInfoList = ddlTableInfo.getDdlIndexInfoList();
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        CheckUtils.notNull(fieldInfoList, "fieldInfoList");
        String dbType = this.opContext.getDbType();
        OpColumnConstraints opColumnConstraints = OpSelector.selectOpCC(opContext);
        List<String> ddlFieldInfoList = ListTs.newList();
        for (DDLFieldInfo ddlFieldInfo : fieldInfoList) {
            // not gen constraint at the column definition
            ddlFieldInfo.setGenConstraint(false);
            String createColumnSql = opColumnConstraints.getCreateColumnSql(ddlFieldInfo);
            ddlFieldInfoList.add(SP.SPACE + createColumnSql);
        }
        OpTableConstraints opTableConstraints = OpSelector.selectOpCT(opContext);
        List<String> tableConstraints = opTableConstraints.getTableConstraints();
        if (CollUtil.isNotEmpty(tableConstraints)) {
            // filter
            ddlFieldInfoList.addAll(ListTs.map(tableConstraints, e -> SP.SPACE + e));
        }
        String join = ListTs.join(",\n", ddlFieldInfoList);
        res.put(CREATE_DEFINITION, join);
        String schema = ddlTableInfo.getSchema();
        String tableName1 = ddlTableInfo.getTableName();
        String tableName = StrUtil.isNotBlank(schema) ? schema + SP.DOT + tableName1 : tableName1;
        res.put(TABLE_NAME, tableName);
        // a most of db support
        if (ddlTableInfo.isTemporary())
            res.put(TEMPORARY, "temporary");
        // oracle not support
        if (ddlTableInfo.isIfNotExists() && !DbType.ORACLE.getDb().equals(dbType))
            res.put(IF_NOT_EXIST, "if not exists");
        // only pg support
        if (ddlTableInfo.isPgUnlogged() && DbType.POSTGRE_SQL.getDb().equals(dbType))
            res.put(UN_LOGGED, "unlogged");
        List<String> tableAttrs = opTableConstraints.getTableOptions();

        if (CollUtil.isNotEmpty(tableAttrs))
            res.put(TABLE_OPTIONS, ListTs.join(SP.NEWLINE, tableAttrs));
        List<String> partitionOptions = opTableConstraints.getPartitionOptions();
        if (CollUtil.isNotEmpty(partitionOptions))
            res.put(PARTITION_OPTIONS, ListTs.join(SP.NEWLINE, partitionOptions));
        return res;
    }

    /**
     * 获取建表ddl语句
     *
     * @author bokun.li
     * @date 2025/8/26
     */
    @Override
    public String getCreateTableDDL() {
        DDLTableInfo ddlTableInfo = opContext.getDdlTableInfo();
        CheckUtils.notNull(ddlTableInfo, "ddlTableInfo");
        OpConfig opConfig = this.getOpContext().getOpConfig();
        return opConfig.patchStrWithTemplate(ddlTableInfo, getTemplate(),FIELD_MAP, extParamMap, this::getTemplateParams);
    }

    /**
     * 获取注释语句 有些数据库可能没有 比如mysql就没有,没有的要重载这个方法返回null,mysql和sqlserver我就直接再这里处理了
     *
     * @return
     */
    @Override
    public List<String> getCreateTableComments() {
        String dbType = this.opContext.getDbType();
        if (ListTs.asList(DbType.MYSQL.getDb(), DbType.SQL_SERVER.getDb()).contains(dbType)) return null;
        OpConfig opConfig = this.opContext.getOpConfig();
        List<String> comments = ListTs.newList();
        DDLTableInfo ddlTableInfo = this.opContext.getDdlTableInfo();
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        String tableComments = ddlTableInfo.getComment();
        Class<?> domainClass = ddlTableInfo.getDomainClass();
        if (null != domainClass && StrUtil.isBlank(tableComments)) {
            if (StrUtil.isBlank(tableComments) && domainClass.isAnnotationPresent(Schema.class)) {
                tableComments = domainClass.getAnnotation(Schema.class).description();
            }
            if (StrUtil.isBlank(tableComments) && domainClass.isAnnotationPresent(Desc.class)) {
                tableComments = domainClass.getAnnotation(Desc.class).value();
            }
        }
        if (StrUtil.isNotBlank(tableComments)) {
            comments.add(String.format("COMMENT ON TABLE %s IS '%s'", opConfig.getTableName(ddlTableInfo), tableComments));
        }
        if (CollUtil.isNotEmpty(fieldInfoList)) {
            for (DDLFieldInfo fieldInfo : fieldInfoList) {
                Class<?> fieldClass = fieldInfo.getFieldClass();
                String comment = fieldInfo.getComment();
                if (null != fieldClass) {
                    if (StrUtil.isBlank(comment) && fieldClass.isAnnotationPresent(Desc.class)) {
                        comment = fieldClass.getAnnotation(Desc.class).value();
                    }
                    if (StrUtil.isBlank(comment) && fieldClass.isAnnotationPresent(Schema.class)) {
                        comment = fieldClass.getAnnotation(Schema.class).description();
                    }
                }
                if (StrUtil.isNotBlank(comment)) {
                    comments.add(String.format("COMMENT ON COLUMN %s IS '%s'", opConfig.getTableName(ddlTableInfo) + SP.DOT + opConfig.getColumnName(fieldInfo.getName()), comment));
                }
            }
        }
        return comments;
    }

    @Override
    public List<String> getIndexList() {
        OpDdlIndex opSqlCommands = OpSelector.selectOpIndex(this.getOpContext());
        List<DDLIndexInfo> ddlIndexInfos = Optional.ofNullable(this.getOpContext())
                .map(OpContext::getDdlTableInfo)
                .map(DDLTableInfo::getDdlIndexInfoList)
                .orElse(ListTs.newList());
        List<String> objects = ListTs.newList();
        if(CollUtil.isNotEmpty(ddlIndexInfos)){
            for (DDLIndexInfo ddlIndexInfo : ddlIndexInfos) {
                String indexes = opSqlCommands.getIndexes(ddlIndexInfo);
                if(StrUtil.isNotBlank(indexes)){
                    objects.add(indexes);
                }
            }
        }
        return objects;
    }
}
