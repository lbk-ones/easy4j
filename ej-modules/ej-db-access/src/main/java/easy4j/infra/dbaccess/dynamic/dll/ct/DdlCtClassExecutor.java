package easy4j.infra.dbaccess.dynamic.dll.ct;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.dynamic.dll.*;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.DDLFieldStrategyExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.DDLFieldStrategySelector;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableCoreSelector;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndex;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.core.annotation.AnnotationUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 根据java类来逆向
 *
 * @author bokun.li
 * @date 2025-08-03
 */
public class DdlCtClassExecutor extends AbstractDDLParseExecutor implements DDLParseExecutor {

    DDLConfig dllConfig;

    public DdlCtClassExecutor(DDLConfig dllConfig) {
        CheckUtils.notNull(dllConfig, "dllConfig");
        CheckUtils.checkByPath(dllConfig, "tableName");
        CheckUtils.checkByPath(dllConfig, "domainClass");
        this.dllConfig = dllConfig;
    }

    public String execute() {
        Class<?> domainClass = this.dllConfig.getDomainClass();
        DDLTableInfo ddlTableInfo = getDdlTableInfo(domainClass);
        ddlTableInfo.setDbVersion(this.dllConfig.getDbVersion());
        ddlTableInfo.setSchema(this.dllConfig.getSchema());
        ddlTableInfo.setDbType(this.dllConfig.getDbType());
        ddlTableInfo.setTableName(this.dllConfig.getTableName());
        ddlTableInfo.setDllConfig(this.dllConfig);
        List<DDLFieldInfo> ddlFieldInfos = getDdlFieldInfoList(domainClass);
        ddlTableInfo.setFieldInfoList(ddlFieldInfos);
        List<DDLIndexInfo> ddlIndexInfos = getIndexInfoList(domainClass);
        ddlTableInfo.setDdlIndexInfoList(ddlIndexInfos);
        DDLTableExecutor dDlTableExecutor = DDLTableCoreSelector.getDDlTableExecutor(ddlTableInfo);
        List<String> objects = ListTs.newLinkedList();
        Set<String> distinctSet = new HashSet<>();
        for (DDLFieldInfo ddlFieldInfo : ddlFieldInfos) {
            String name = ddlFieldInfo.getName();
            String underlineCase = StrUtil.toUnderlineCase(name);
            if (distinctSet.contains(underlineCase)) {
                continue;
            } else {
                distinctSet.add(underlineCase);
            }
            ddlFieldInfo.setDllConfig(this.dllConfig);
            ddlFieldInfo.setDbType(this.dllConfig.getDbType());
            DDLFieldStrategyExecutor ddlFieldCoreContext = DDLFieldStrategySelector.selectExecutor(ddlFieldInfo);
            String fieldStrList = ddlFieldCoreContext.executor();
            objects.add(fieldStrList);
        }
        return getString(dDlTableExecutor, objects);
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


    private DDLTableInfo getDdlTableInfo(Class<?> aclass) {
        String tableName = this.dllConfig.getTableName();
        String dbType = this.dllConfig.getDbType();
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName(tableName);
        ddlTableInfo.setDbType(dbType);
        ddlTableInfo.setDomainClass(aclass);
        DDLTable annotation = aclass.getAnnotation(DDLTable.class);
        if (null != annotation) {
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
            ddlTableInfo = BeanUtil.mapToBean(annotationAttributes, DDLTableInfo.class, true, CopyOptions.create().ignoreError());
        }
        return ddlTableInfo;
    }

    private List<DDLFieldInfo> getDdlFieldInfoList(Class<?> aclass) {
        Object newInstance = ReflectUtil.newInstance(aclass);
        List<DDLFieldInfo> objects = ListTs.newLinkedList();
        Field[] fields = ReflectUtil.getFields(aclass);
        for (Field field : fields) {
            if (!skipColumn(field)) {
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
    private void fieldMetaInfoExtraParse(Object newInstance, Field field, DDLFieldInfo ddlFieldInfo) {
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
