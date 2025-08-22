package easy4j.infra.dbaccess.dynamic.dll.op.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.dll.DDLField;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTable;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndex;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.MetaInfoParse;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.core.annotation.AnnotationUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JavaClassMetaInfoParse implements MetaInfoParse {

    OpContext opContext;

    @Override
    public void setOpContext(OpContext opContext) {
        this.opContext = opContext;
    }

    @Override
    public DDLTableInfo parse() {
        try{
            Class<?> domainClass = this.opContext.getDomainClass();
            DDLTableInfo ddlTableInfo = getDdlTableInfo(domainClass);
            ddlTableInfo.setDbVersion(this.opContext.getDbVersion());
            ddlTableInfo.setSchema(this.opContext.getSchema());
            ddlTableInfo.setDbType(this.opContext.getDbType());
            ddlTableInfo.setTableName(this.opContext.getTableName());

            List<DDLFieldInfo> ddlFieldInfos = getDdlFieldInfoList(domainClass);
            ddlTableInfo.setFieldInfoList(ddlFieldInfos);
            List<DDLIndexInfo> ddlIndexInfos = getIndexInfoList(domainClass);
            ddlTableInfo.setDdlIndexInfoList(ddlIndexInfos);

            return ddlTableInfo;
        }catch (SQLException e){
            throw JdbcHelper.translateSqlException("DDLTableInfo Parse","",e);
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


    private DDLTableInfo getDdlTableInfo(Class<?> aclass) throws SQLException {

        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName(getTableName(aclass));
        String databaseType = JdbcHelper.getDatabaseType(opContext.getConnection());
        ddlTableInfo.setDbType(databaseType);
        ddlTableInfo.setDomainClass(aclass);
        DDLTable annotation = aclass.getAnnotation(DDLTable.class);
        if (null != annotation) {
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
            ddlTableInfo = BeanUtil.mapToBean(annotationAttributes, DDLTableInfo.class, true, CopyOptions.create().ignoreError());
        }
        return ddlTableInfo;
    }

    private String getTableName(Class<?> aclass) {
        CommonDBAccess commonDBAccess = this.opContext.getOpConfig().getCommonDBAccess();
        Dialect dialect = this.opContext.getDialect();

        return getDDLTableName(dialect,aclass,commonDBAccess.getTableName(aclass, dialect));
    }

    private String getDDLTableName(Dialect dialect, Class<?> aclass, String tableName) {
        CommonDBAccess commonDBAccess = this.opContext.getOpConfig().getCommonDBAccess();
        boolean annotationPresent = aclass.isAnnotationPresent(DDLTable.class);
        if (annotationPresent) {
            Wrapper wrapper = dialect.getWrapper();
            try {
                char preWrapQuote = wrapper.getPreWrapQuote();
                char sufWrapQuote = wrapper.getSufWrapQuote();
                tableName = StrUtil.unWrap(tableName, preWrapQuote, sufWrapQuote);
            } catch (Exception ignored) {
            }

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

    private List<DDLFieldInfo> getDdlFieldInfoList(Class<?> aclass) {
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
                    fieldMetaInfoExtraParse(newInstance, field, ddlFieldInfo1);
                    objects.add(ddlFieldInfo1);
                } else {
                    DDLFieldInfo ddlFieldInfo = new DDLFieldInfo();
                    fieldMetaInfoExtraParse(newInstance, field, ddlFieldInfo);
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
    public static void fieldMetaInfoExtraParse(Object newInstance, Field field, DDLFieldInfo ddlFieldInfo) {
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
    }
}
