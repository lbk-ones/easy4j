package easy4j.infra.dbaccess.dynamic.dll.ct;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.dynamic.dll.*;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.DDLFieldStrategyExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.DDLFieldStrategySelector;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableCoreSelector;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        ddlTableInfo.setDllConfig(this.dllConfig);
        List<DDLFieldInfo> ddlFieldInfos = getDdlFieldInfoList(domainClass);
        ddlTableInfo.setFieldInfoList(ddlFieldInfos);
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


    private DDLTableInfo getDdlTableInfo(Class<?> aclass) {
        String tableName = this.dllConfig.getTableName();
        String dbType = this.dllConfig.getDbType();
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName(tableName);
        ddlTableInfo.setDbType(dbType);
        DDLTable annotation = aclass.getAnnotation(DDLTable.class);
        if (null != annotation) {
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
            ddlTableInfo = BeanUtil.mapToBean(annotationAttributes, DDLTableInfo.class, true, CopyOptions.create().ignoreError());
        }
        return ddlTableInfo;
    }

    private List<DDLFieldInfo> getDdlFieldInfoList(Class<?> aclass) {
        List<DDLFieldInfo> objects = ListTs.newLinkedList();
        Field[] fields = ReflectUtil.getFields(aclass);
        for (Field field : fields) {
            if (!skipColumn(field)) {
                Class<?> type = field.getType();
                DDLField annotation = field.getAnnotation(DDLField.class);
                if (null != annotation) {
                    Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
                    DDLFieldInfo ddlFieldInfo1 = BeanUtil.mapToBean(annotationAttributes, DDLFieldInfo.class, true, CopyOptions.create().ignoreError());
                    ddlFieldInfo1.setFieldClass(type);
                    String comment = ddlFieldInfo1.getComment();
                    if (StrUtil.isBlank(comment) && field.isAnnotationPresent(Desc.class))
                        comment = field.getAnnotation(Desc.class).value();
                    ddlFieldInfo1.setComment(comment);
                    objects.add(ddlFieldInfo1);
                } else {
                    String name = field.getName();
                    JdbcColumn jdbcColumn = field.getAnnotation(JdbcColumn.class);
                    if (null != jdbcColumn) name = jdbcColumn.name();
                    DDLFieldInfo ddlFieldInfo = new DDLFieldInfo();
                    ddlFieldInfo.setFieldClass(type);
                    ddlFieldInfo.setName(name);
                    if (field.isAnnotationPresent(Desc.class)) {
                        ddlFieldInfo.setComment(field.getAnnotation(Desc.class).value());
                    }
                    objects.add(ddlFieldInfo);
                }
            }
        }
        return objects;
    }


}
