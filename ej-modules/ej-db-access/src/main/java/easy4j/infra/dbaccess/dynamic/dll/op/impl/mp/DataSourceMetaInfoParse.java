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
import com.google.common.collect.Maps;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.*;
import easy4j.infra.dbaccess.dynamic.dll.idx.IndexType;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndex;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.MetaInfoParse;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.sc.AbstractOpSqlCommands;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.sc.CopyDbConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.*;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.AnnotationUtils;

import javax.sql.DataSource;

import jakarta.validation.constraints.NotNull;

import java.sql.SQLException;
import java.util.*;

/**
 * DataSourceMetaInfoParse
 * 从数据源中解析表信息
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class DataSourceMetaInfoParse implements MetaInfoParse {

    /**
     * 从构造函数传入的上下文
     */
    @Getter
    OpContext opContext;

    /**
     * 从构造函数传入的数据源
     */
    @Setter
    DataSource dataSource;

    /**
     * 从构造函数传入的表名
     */
    @Setter
    String tableName;


    /**
     * 在copy数据源的时候会用到这个字段
     *
     * @see AbstractOpSqlCommands#copyDataSourceDDL(String[], String[], CopyDbConfig)
     */
    @Setter
    String copyTargetDbType;

    /**
     * 是否转义表名
     */
    @Setter
    boolean escapeTableName;

    public DataSourceMetaInfoParse() {
    }

    public DataSourceMetaInfoParse(@NotNull DataSource dataSource, @NotNull String tableName, @NotNull OpContext opContext) {
        CheckUtils.notNull(dataSource, "dataSource");
        CheckUtils.notNull(tableName, "tableName");
        this.opContext = opContext;
        this.dataSource = dataSource;
        this.tableName = tableName;
    }

    public DataSourceMetaInfoParse(@NotNull String tableName, @NotNull OpContext opContext) {
        CheckUtils.notNull(tableName, "tableName");
        CheckUtils.checkByPath(opContext, "dataSource");
        this.opContext = opContext;
        this.tableName = tableName;
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

    /**
     * 解析索引注解
     *
     * @author bokun.li
     * @date 2025/8/19
     */
    private List<DDLIndexInfo> getIndexInfoList(Class<?> domainClass) {
        List<DDLIndexInfo> objects = ListTs.newList();
        if (domainClass.isAnnotationPresent(DDLTable.class)) {
            DDLTable annotation = domainClass.getAnnotation(DDLTable.class);
            DDLIndex[] indexes = annotation.indexes();
            for (DDLIndex index : indexes) {
                Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(index);
                DDLIndexInfo ddlIndexInfo = BeanUtil.mapToBean(annotationAttributes, DDLIndexInfo.class, true, CopyOptions.create().ignoreError());
                ddlIndexInfo.setDdlIndex(index);
                objects.add(ddlIndexInfo);
            }
        }
        return objects;
    }


    private DDLTableInfo getDdlTableInfo() throws SQLException {
        CheckUtils.notNull(this.tableName);
        //CheckUtils.checkByLambda(this.opContext, OpContext::getDdlTableInfo);
        DialectV2 opDbMeta = DialectFactory.get(this.opContext.getConnection());
        List<DatabaseColumnMetadata> columns = opDbMeta.getColumnsNoCache(this.opContext.getConnectionCatalog(), this.opContext.getConnectionSchema(), this.tableName);
        List<PrimaryKeyMetadata> primaryKes = opDbMeta.getPrimaryKes(this.opContext.getConnectionCatalog(), this.opContext.getConnectionSchema(), this.tableName);// 转换索引列信息
        List<IndexInfoMetaInfo> indexInfoMetaInfos = opDbMeta.getIndexInfos(this.opContext.getConnectionCatalog(), this.opContext.getConnectionSchema(), this.tableName);

        Map<String, PrimaryKeyMetadata> primaryKeyMetadataMap = ListTs.toMap(primaryKes, PrimaryKeyMetadata::getColumnName);
        Map<String, IndexInfoMetaInfo> indexInfoMetaInfoMap = ListTs.toMap(indexInfoMetaInfos, IndexInfoMetaInfo::getColumnName);
        String dbType = this.opContext.getDbType();
        String dbVersion = this.opContext.getDbVersion();
        OpConfig opConfig = this.opContext.getOpConfig();
        String schema = this.opContext.getSchema();
        // 转换字段列信息
        List<DDLFieldInfo> map = ListTs.map(columns, e -> getDdlFieldInfoFromColumnMeta(e, dbType, dbVersion, opConfig, primaryKeyMetadataMap, indexInfoMetaInfoMap));

        // 主键排在前面
        map.sort((o1, o2) -> {
            Integer primary = o1.isPrimary() ? 1 : 0;
            Integer primary2 = o2.isPrimary() ? 1 : 0;
            return primary2.compareTo(primary);
        });

        Map<String, List<IndexInfoMetaInfo>> stringListMap = ListTs.groupBy(indexInfoMetaInfos, IndexInfoMetaInfo::getIndexName);

        Map<String, List<IndexInfoMetaInfo>> indexMap = ListTs.groupBy(indexInfoMetaInfos, IndexInfoMetaInfo::getColumnName);

        Map<String, Boolean> existMap = Maps.newHashMap();

        List<DDLIndexInfo> indexInfos = ListTs.map(indexInfoMetaInfos, e -> getDdlIndexInfoFromIndexMeta(e, stringListMap, schema, primaryKeyMetadataMap, indexMap, existMap));

        existMap.clear();
        indexMap.clear();
        stringListMap.clear();
        primaryKeyMetadataMap.clear();
        indexInfoMetaInfoMap.clear();

        this.opContext.setDbColumns(columns);
        this.opContext.setPrimaryKes(primaryKes);
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setEscapeTableName(this.escapeTableName);
        ddlTableInfo.setDbType(dbType);
        ddlTableInfo.setDbVersion(this.opContext.getDbVersion());
        ddlTableInfo.setTableName(tableName);
        ddlTableInfo.setDdlIndexInfoList(indexInfos);
        ddlTableInfo.setSchema(this.opContext.getSchema());
        map.forEach(e -> {
            e.setTableName(ddlTableInfo.getTableName());
            e.setSchema(ddlTableInfo.getSchema());
        });
        ddlTableInfo.setFieldInfoList(map);
        //ddlTableInfo.setDllConfig(new DDLConfig());
        ddlTableInfo.setOpConfig(this.opContext.getOpConfig());
        List<TableMetadata> tableInfos = opDbMeta.getTableInfos(tableName);
        TableMetadata tableMetadata = ListTs.get(tableInfos, 0);
        if (null != tableMetadata) {
            ddlTableInfo.setComment(tableMetadata.getRemarks());
        }
        List<TableMetadata> tableInfos1 = opDbMeta.getTableInfos(ddlTableInfo.getTableName());
        this.opContext.setTableMetadata(ListTs.get(tableInfos1, 0));


        return ddlTableInfo;
    }

    /**
     * getDdlIndexInfoFromIndexMeta
     * fix: primary key can't create index
     *
     * @param e             index info meta
     * @param stringListMap group byu indexInfoMetaInfo
     * @param schema        schema info
     * @return
     */
    private DDLIndexInfo getDdlIndexInfoFromIndexMeta(IndexInfoMetaInfo e, Map<String, List<IndexInfoMetaInfo>> stringListMap, String schema, Map<String, PrimaryKeyMetadata> primaryKeyMetadataMap, Map<String, List<IndexInfoMetaInfo>> indexMap, Map<String, Boolean> existMap) {
        String columnName = e.getColumnName();
        PrimaryKeyMetadata primaryKeyMetadata = primaryKeyMetadataMap.get(columnName);
        if (null != primaryKeyMetadata) {
            return null;
        }
        // unique index 交给表约束实现
        if (!e.isNonUnique()) {
            return null;
        } else {
            // 如果普通索引被唯一索引索引过那么这里也跳过
            List<IndexInfoMetaInfo> indexInfoMetaInfos = indexMap.get(columnName);
            if (CollUtil.isNotEmpty(indexInfoMetaInfos)) {
                if (indexInfoMetaInfos.stream().anyMatch(e2 -> !e2.isNonUnique())) {
                    return null;
                }
            }
            // 当然 也不能重复
            Boolean b = existMap.get(columnName);
            if (b != null && b) {
                return null;
            }
            existMap.put(columnName, true);
        }
        String indexName = e.getIndexName();
        List<IndexInfoMetaInfo> orDefault = stringListMap.getOrDefault(indexName, ListTs.newList());
        if (CollUtil.isNotEmpty(orDefault)) {
            orDefault.sort(Comparator.comparingInt(IndexInfoMetaInfo::getOrdinalPosition));
        }
        DDLIndexInfo ddlIndexInfo = new DDLIndexInfo();
        ddlIndexInfo.setSchema(schema);
        ddlIndexInfo.setTableName(tableName);
        ddlIndexInfo.setName(e.getIndexName());
        ddlIndexInfo.setIndexTypeName("");
        ddlIndexInfo.setIndexNamePrefix("");
        ddlIndexInfo.setUsing("");
        ddlIndexInfo.setKeys(ListTs.mapToList(orDefault, IndexInfoMetaInfo::getColumnName).toArray(new String[]{}));
        ddlIndexInfo.setType(IndexType.BTREE);
        ddlIndexInfo.setArgs(new String[0]);
//            ddlIndexInfo.setDdlIndex(new DDLIndex());
        return ddlIndexInfo;
    }

    /**
     * getDdlFieldInfoFromColumnMeta
     *
     * @param e                     meta info
     * @param dbType                db type
     * @param dbVersion             db version
     * @param opConfig              op config
     * @param primaryKeyMetadataMap primarykey meta Info map
     * @param indexInfoMetaInfoMap  indexinfo meta info map
     * @return
     */
    private DDLFieldInfo getDdlFieldInfoFromColumnMeta(DatabaseColumnMetadata e, String dbType, String dbVersion, OpConfig opConfig, Map<String, PrimaryKeyMetadata> primaryKeyMetadataMap, Map<String, IndexInfoMetaInfo> indexInfoMetaInfoMap) {
        String typeName = e.getTypeName();
        String columnName = e.getColumnName();
        int columnSize = e.getColumnSize();
        String columnDef = e.getColumnDef();
        Integer decimalDigits = e.getDecimalDigits();
        DDLFieldInfo ddlFieldInfo = new DDLFieldInfo();
        ddlFieldInfo.setDbType(dbType);
        ddlFieldInfo.setDbVersion(dbVersion);
        DialectV2 dialectV2 = DialectFactory.get(this.opContext.getConnection());
        ddlFieldInfo.setFieldClass(dialectV2.getJavaClassByTypeNameAndDbType(typeName));
        ddlFieldInfo.setName(columnName);
        ddlFieldInfo.setPrimary(opConfig.isMatchMapIgnoreCase(primaryKeyMetadataMap, columnName));
        ddlFieldInfo.setAutoIncrement("YES".equals(e.getIsAutoincrement()));
        ddlFieldInfo.setDataType(typeName);
        ddlFieldInfo.setDataLength(columnSize);
        if (null != decimalDigits) {
            ddlFieldInfo.setDataDecimal(decimalDigits);
        }
        ddlFieldInfo.setDataTypeAttr(new String[0]);
        if (StrUtil.isNotBlank(columnDef)) {
            if (StrUtil.containsIgnoreCase(columnDef, "nextval")) {
                columnDef = "";
            }
        }

        ddlFieldInfo.setDef(columnDef);
        ddlFieldInfo.setDefNum(-1);
        ddlFieldInfo.setDefTime(false);
        ddlFieldInfo.setNotNull(e.getNullable() == 0 || "NO".equals(e.getIsNullable()));
        IndexInfoMetaInfo matchMapIgnoreCase = opConfig.getMatchMapIgnoreCase(indexInfoMetaInfoMap, columnName);
        if (!ddlFieldInfo.isPrimary()) {
            ddlFieldInfo.setUnique(matchMapIgnoreCase != null && !matchMapIgnoreCase.isNonUnique());
        }
        ddlFieldInfo.setCheck(null);
        ddlFieldInfo.setIndex(matchMapIgnoreCase != null);
        ddlFieldInfo.setConstraint(new String[0]);
        ddlFieldInfo.setComment(e.getRemarks());
        boolean lob = dialectV2.isLob(typeName);
        // 如果copy的目标库是oracle 那么主键是不能是clob大字段类型
        if (lob && DbType.ORACLE.getDb().equals(this.copyTargetDbType) && ddlFieldInfo.isPrimary()) {
            ddlFieldInfo.setLob(false);
            ddlFieldInfo.setFieldClass(String.class);
            ddlFieldInfo.setDataType(OracleFieldType.VARCHAR2.getFieldType());
            ddlFieldInfo.setDataLength(255);
        } else if (lob && DbType.SQL_SERVER.getDb().equals(this.copyTargetDbType) && ddlFieldInfo.isPrimary()) {
            ddlFieldInfo.setLob(false);
            ddlFieldInfo.setFieldClass(String.class);
            ddlFieldInfo.setDataType(SqlServerFieldType.NVARCHAR.getFieldType());
            ddlFieldInfo.setDataLength(255);
        } else if (lob && DbType.H2.getDb().equals(this.copyTargetDbType) && ddlFieldInfo.isPrimary()) {
            ddlFieldInfo.setLob(false);
            ddlFieldInfo.setFieldClass(String.class);
            ddlFieldInfo.setDataType(H2SqlFieldType.VARCHAR.getFieldType());
            ddlFieldInfo.setDataLength(255);
        } else {
            ddlFieldInfo.setLob(lob);
        }
        ddlFieldInfo.setJson(dialectV2.isJson(typeName));
        ddlFieldInfo.setGenConstraint(false);
        ddlFieldInfo.setDllConfig(new DDLConfig());
        IndexInfoMetaInfo indexInfoMetaInfo = new IndexInfoMetaInfo();
        indexInfoMetaInfo.setIndexName("none");
        indexInfoMetaInfo.setOrdinalPosition((short) 0);
        ddlFieldInfo.setIndexName(indexInfoMetaInfoMap.getOrDefault(columnName, indexInfoMetaInfo).getIndexName());
        ddlFieldInfo.setIndexSort(indexInfoMetaInfoMap.getOrDefault(columnName, indexInfoMetaInfo).getOrdinalPosition());
        return ddlFieldInfo;
    }
}
