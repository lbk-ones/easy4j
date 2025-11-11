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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.collect.Maps;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.DDLField;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTable;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndex;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.MetaInfoParse;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.*;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JavaClassMetaInfoParse
 * 从java类中解析表信息
 *
 * @author bokun.li
 * @date 2025/8/23
 */
@Getter
public class JavaClassMetaInfoParse implements MetaInfoParse {

    OpContext opContext;

    public JavaClassMetaInfoParse() {
    }

    public JavaClassMetaInfoParse(OpContext opContext) {
        this.opContext = opContext;
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
        CheckUtils.checkByLambda(this.opContext, OpContext::getDomainClass);
        Class<?> aclass = this.opContext.getDomainClass();
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        DDLTable annotation = aclass.getAnnotation(DDLTable.class);
        if (null != annotation) {
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
            ddlTableInfo = BeanUtil.mapToBean(annotationAttributes, DDLTableInfo.class, true, CopyOptions.create().ignoreError());
        }
        ddlTableInfo.setTableName(getTableName(aclass));
        DialectV2 opDbMeta = DialectFactory.get(this.opContext.getConnection());
        List<DatabaseColumnMetadata> columns = opDbMeta.getColumns(this.opContext.getConnectionCatalog(), this.opContext.getConnectionSchema(), ddlTableInfo.getTableName());
        this.opContext.setDbColumns(columns);
        List<PrimaryKeyMetadata> primaryKes = opDbMeta.getPrimaryKes(this.opContext.getConnectionCatalog(), this.opContext.getConnectionSchema(), ddlTableInfo.getTableName());
        this.opContext.setPrimaryKes(primaryKes);
        ddlTableInfo.setDomainClass(aclass);

        ddlTableInfo.setDbVersion(this.opContext.getDbVersion());
        ddlTableInfo.setSchema(this.opContext.getSchema());
        ddlTableInfo.setDbType(this.opContext.getDbType());
        List<DDLIndexInfo> ddlIndexInfos = getIndexInfoList(aclass);
        Map<String, DDLIndexInfo> columnVsIndexMap = Maps.newHashMap();

        // handler index info
        handlerIndexInfo(ddlIndexInfos, ddlTableInfo, columnVsIndexMap);

        ddlTableInfo.setDdlIndexInfoList(ddlIndexInfos);
        List<DDLFieldInfo> ddlFieldInfos = getDdlFieldInfoList(ddlTableInfo, aclass, columnVsIndexMap);
        ddlTableInfo.setFieldInfoList(ddlFieldInfos);

        List<TableMetadata> tableInfos1 = opDbMeta.getTableInfos(ddlTableInfo.getTableName());
        this.opContext.setTableMetadata(ListTs.get(tableInfos1, 0));
        return ddlTableInfo;
    }

    private void handlerIndexInfo(List<DDLIndexInfo> ddlIndexInfos, DDLTableInfo ddlTableInfo, Map<String, DDLIndexInfo> columnVsIndexMap) {
        OpConfig opConfig = this.getOpContext().getOpConfig();
        for (DDLIndexInfo ddlIndexInfo : ddlIndexInfos) {
            ddlIndexInfo.setTableName(ddlTableInfo.getTableName());
            String[] keys1 = ddlIndexInfo.getKeys();
            String idname = opConfig.compatibleGetIdxName(ddlIndexInfo, keys1, ddlIndexInfo.getIndexTypeName(), opConfig, ddlIndexInfo.getName());
            ddlIndexInfo.setName(idname);
            String[] keys = ddlIndexInfo.getKeys();
            if (ListTs.isNotEmpty(keys)) {
                for (String key : keys) {
                    columnVsIndexMap.put(key, ddlIndexInfo);
                }
            }
        }
    }

    private String getTableName(Class<?> aclass) {
        CommonDBAccess commonDBAccess = this.opContext.getOpConfig().getCommonDBAccess();
        Dialect dialect = this.opContext.getDialect();

        return getDDLTableName(dialect, aclass, commonDBAccess.getTableName(aclass, dialect));
    }

    private String getDDLTableName(Dialect dialect, Class<?> aclass, String tableName) {
        CommonDBAccess commonDBAccess = this.opContext.getOpConfig().getCommonDBAccess();
        boolean annotationPresent = aclass.isAnnotationPresent(DDLTable.class);
        if (annotationPresent) {
            tableName = dialect.getWrapper().wrap(tableName);

            DDLTable annotation = aclass.getAnnotation(DDLTable.class);
            String s = annotation.tableName();
            String fName;
            if (StrUtil.isNotBlank(s)) {
                fName = s;
            } else {
                fName = tableName;
            }
            if (this.opContext.getOpConfig().isToUnderLine()) {
                fName = commonDBAccess.toUnderLine(fName);
            }
            return fName;
        } else {
            if (aclass.isAnnotationPresent(JdbcTable.class)) {
                JdbcTable annotation = aclass.getAnnotation(JdbcTable.class);
                String name = annotation.name();
                if (StrUtil.isNotBlank(name)) return name;
            } else if (aclass.isAnnotationPresent(TableName.class)) {
                TableName tableName1 = aclass.getAnnotation(TableName.class);
                String name = tableName1.value();
                if (StrUtil.isNotBlank(name)) return name;
            } else if (aclass.isAnnotationPresent(Table.class)) {
                Table table = aclass.getAnnotation(Table.class);
                String name = table.name();
                if (StrUtil.isNotBlank(name)) return name;
            }
            String simpleName = aclass.getSimpleName();
            return StrUtil.toUnderlineCase(simpleName);
        }
//        return null;
    }

    private List<DDLFieldInfo> getDdlFieldInfoList(DDLTableInfo ddlTableInfo, Class<?> aclass, Map<String, DDLIndexInfo> columnVsIndexMap) {
        Object newInstance = ReflectUtil.newInstance(aclass);
        List<DDLFieldInfo> objects = ListTs.newLinkedList();
        Field[] fields = ReflectUtil.getFields(aclass);
        CommonDBAccess commonDBAccess = this.opContext.getOpConfig().getCommonDBAccess();
        for (Field field : fields) {
            if (!commonDBAccess.skipColumn(field)) {
                DDLField annotation = field.getAnnotation(DDLField.class);
                if (null != annotation) {
                    Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
                    DDLFieldInfo ddlFieldInfo1 = BeanUtil.mapToBean(annotationAttributes, DDLFieldInfo.class, true, CopyOptions.create().ignoreError());
                    fieldMetaInfoExtraParse(ddlTableInfo, newInstance, field, ddlFieldInfo1, columnVsIndexMap);
                    objects.add(ddlFieldInfo1);
                } else {
                    DDLFieldInfo ddlFieldInfo = new DDLFieldInfo();
                    fieldMetaInfoExtraParse(ddlTableInfo, newInstance, field, ddlFieldInfo, columnVsIndexMap);
                    objects.add(ddlFieldInfo);
                }
            }
        }
        return objects;
    }

    /**
     * handler field meta info
     * mybatis-plus jpa
     * don't parse DDLField annotation
     *
     * @author bokun.li
     * @date 2025/8/20
     */
    public static void fieldMetaInfoExtraParse(DDLTableInfo ddlTableInfo, Object newInstance, Field field, DDLFieldInfo ddlFieldInfo, Map<String, DDLIndexInfo> columnVsIndexMap) {
        Class<?> type = field.getType();
        ddlFieldInfo.setFieldClass(type);
        // comment
        String comment = ddlFieldInfo.getComment();
        if (StrUtil.isBlank(comment) && field.isAnnotationPresent(Desc.class))
            comment = field.getAnnotation(Desc.class).value();
        if (StrUtil.isBlank(comment) && field.isAnnotationPresent(Schema.class))
            comment = field.getAnnotation(Schema.class).description();
        ddlFieldInfo.setComment(comment);

        // mybatis-plus compatible
        {
            if (StrUtil.isBlank(ddlFieldInfo.getName()) && field.isAnnotationPresent(TableField.class))
                ddlFieldInfo.setName(field.getAnnotation(TableField.class).value());
            // is-auto-increment
            if (!ddlFieldInfo.isAutoIncrement()) {
                if (field.isAnnotationPresent(TableId.class)) {
                    TableId annotation = field.getAnnotation(TableId.class);
                    if (annotation.type() == IdType.AUTO) {
                        ddlFieldInfo.setAutoIncrement(true);
                    }
                }
            }
            if (!ddlFieldInfo.isPrimary() && field.isAnnotationPresent(TableId.class)) {
                ddlFieldInfo.setPrimary(true);
            }
        }

        // jdbc-column compatible
        {
            if (field.isAnnotationPresent(JdbcColumn.class)) {
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                if (!ddlFieldInfo.isAutoIncrement() && annotation.autoIncrement()) ddlFieldInfo.setAutoIncrement(true);

                if (StrUtil.isBlank(ddlFieldInfo.getName()))
                    ddlFieldInfo.setName(field.getAnnotation(JdbcColumn.class).name());

                if (!ddlFieldInfo.isPrimary() && annotation.isPrimaryKey()) ddlFieldInfo.setPrimary(true);

                if (annotation.toJson()) ddlFieldInfo.setJson(true);

                String s = annotation.pgType();
                if (StrUtil.isNotBlank(s)) ddlFieldInfo.setDataType(s);
            }
        }

        // jpa compatible
        {
            if (field.isAnnotationPresent(Column.class)) {
                Column annotation = field.getAnnotation(Column.class);
                if (StrUtil.isBlank(ddlFieldInfo.getName()))
                    ddlFieldInfo.setName(field.getAnnotation(Column.class).name());
                int length = annotation.length();
                ddlFieldInfo.setDataLength(length);
                int precision = annotation.precision();
                int scale = annotation.scale();
                if (precision > 0 && ddlFieldInfo.getDataLength() == 0) {
                    ddlFieldInfo.setDataLength(precision);
                }
                if (scale > 0 && ddlFieldInfo.getDataDecimal() == 0) {
                    ddlFieldInfo.setDataDecimal(scale);
                }
                if (!annotation.nullable()) {
                    ddlFieldInfo.setNotNull(true);
                }
                if (annotation.unique()) {
                    ddlFieldInfo.setUnique(true);
                }
                String s = annotation.columnDefinition();
                if (StrUtil.isNotBlank(s)) {
                    if (s.contains(SP.LEFT_BRACKET) && s.contains(SP.RIGHT_BRACKET)) {
                        try {
                            String dataType = s.substring(0, s.indexOf(SP.LEFT_BRACKET));
                            ddlFieldInfo.setDataType(dataType);
                            String dataLengthAndScale = s.substring(s.indexOf(SP.LEFT_BRACKET));
                            String s1 = StrUtil.unWrap(dataLengthAndScale, SP.LEFT_BRACKET, SP.RIGHT_BRACKET);
                            String[] split = s1.split(SP.COMMA);
                            for (int i = 0; i < split.length; i++) {
                                String s2 = split[i];
                                String trim = StrUtil.trim(s2);
                                if (i == 0 && ddlFieldInfo.getDataLength() == 0)
                                    ddlFieldInfo.setDataLength(Integer.parseInt(trim));
                                if (i == 1 && ddlFieldInfo.getDataDecimal() == 0)
                                    ddlFieldInfo.setDataDecimal(Integer.parseInt(trim));
                            }
                        } catch (Exception ignored) {
                        }
                    } else {
                        ddlFieldInfo.setDataType(s);
                    }
                }
            }

            // long text
            if (!ddlFieldInfo.isLob() && field.isAnnotationPresent(Lob.class)) {
                ddlFieldInfo.setLob(true);
            }

            if (!ddlFieldInfo.isPrimary() && field.isAnnotationPresent(Id.class)) {
                ddlFieldInfo.setPrimary(true);
            }
        }

        // get default value
        Object fieldValue = ReflectUtil.getFieldValue(newInstance, field);
        if (null != fieldValue) {
            if (fieldValue instanceof CharSequence) {
                ddlFieldInfo.setDef(fieldValue.toString());
            }
            if (type == int.class || type == Integer.class) {
                int fieldValue1 = (int) fieldValue;
                ddlFieldInfo.setDefNum(fieldValue1);
            }
            if (fieldValue instanceof Date) {
                ddlFieldInfo.setDefTime(true);
            }
        }
        if (StrUtil.isBlank(ddlFieldInfo.getName())) {
            ddlFieldInfo.setName(field.getName());
        }
        // patch index name
        String name = ddlFieldInfo.getName();
        DDLIndexInfo ddlIndexInfo = columnVsIndexMap.get(name);
        if (null != ddlIndexInfo) {
            ddlFieldInfo.setIndexName(ddlIndexInfo.getName());
            String[] keys = ddlIndexInfo.getKeys();
            int i = ListTs.asList(keys).indexOf(name);
            ddlFieldInfo.setIndexSort(i == -1 ? 0 : (short) i);
        } else {
            ddlFieldInfo.setIndexName("none");
            ddlFieldInfo.setIndexSort((short) 0);
        }
        // primary key no need add not null segment
        if (ddlFieldInfo.isPrimary()) {
            ddlFieldInfo.setNotNull(false);
        }
        // patch tableName schema
        ddlFieldInfo.setTableName(ddlTableInfo.getTableName());
        ddlFieldInfo.setSchema(ddlTableInfo.getSchema());

        ddlFieldInfo.setDbVersion(ddlTableInfo.getDbVersion());
    }
}
