package easy4j.infra.dbaccess.orm.vendor;

import cn.hutool.core.map.reference.WeakKeyConcurrentMap;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcIgnore;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.infra.dbaccess.dll.DDLField;
import easy4j.infra.dbaccess.dll.DDLTable;
import jakarta.persistence.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;

public class DefaultVendorSpi extends AbsVendorSpi {
    private static final String NA = "df-";

    private static final WeakKeyConcurrentMap<String, Object> CACHE = new WeakKeyConcurrentMap<>();

    @Override
    public Integer getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isPk(Field field) {
        String s = NA + field.getType().getName() + SP.HASH + field.getName() + SP.HASH + "isPk";
        return (boolean) CACHE.computeIfAbsent(s, object -> {
            boolean isPk = false;
            if (field.isAnnotationPresent(JdbcColumn.class)) {
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                isPk = annotation.isPrimaryKey();
            }
            if (!isPk && field.isAnnotationPresent(Id.class)) {
                isPk = true;
            }
            if (!isPk && field.isAnnotationPresent(DDLField.class)) {
                isPk = field.getAnnotation(DDLField.class).isPrimary();
            }
            return isPk;
        });

    }

    @Override
    public String getColumnName(Field field) {
        String s = NA + field.getType().getName() + SP.HASH + field.getName() + SP.HASH + "getColumnName";
        return (String) CACHE.computeIfAbsent(s, object -> {
            String rn = null;
            if (field.isAnnotationPresent(DDLField.class)) {
                rn = field.getAnnotation(DDLField.class).name();
            }
            if (StrUtil.isBlank(rn) && field.isAnnotationPresent(JdbcColumn.class)) {
                rn = field.getAnnotation(JdbcColumn.class).name();
            }
            if (StrUtil.isBlank(rn) && field.isAnnotationPresent(Column.class)) {
                rn = field.getAnnotation(Column.class).name();
            }
            return rn;
        });

    }

    @Override
    public boolean isAutoIncrement(Field field) {
        String s = NA + field.getType().getName() + SP.HASH + field.getName() + SP.HASH + "isAutoIncrement";
        return (boolean) CACHE.computeIfAbsent(s, object -> {
            boolean isAuto = false;
            if (field.isAnnotationPresent(JdbcColumn.class)) {
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                isAuto = annotation.isPrimaryKey() && annotation.autoIncrement();
            }
            if (!isAuto && field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(GeneratedValue.class) && field.getAnnotation(GeneratedValue.class).strategy() == GenerationType.AUTO) {
                isAuto = true;
            }
            if (!isAuto && field.isAnnotationPresent(DDLField.class)) {
                DDLField annotation = field.getAnnotation(DDLField.class);
                isAuto = annotation.isPrimary() && annotation.isAutoIncrement();
            }
            return isAuto;
        });

    }

    @Override
    public String getSchema(Class<?> clazz) {
        String s = NA + clazz.getName() + SP.HASH + "getSchema";
        return (String) CACHE.computeIfAbsent(s, object -> {
            JdbcTable annotation = clazz.getAnnotation(JdbcTable.class);
            String schema = null;
            if (null != annotation && StrUtil.isNotBlank(annotation.name())) {
                schema = annotation.schema();
            }
            return schema;
        });

    }

    @Override
    public String getTableName(Class<?> clazz) {
        String s = NA + clazz.getName() + SP.HASH + "getTableName";
        return (String) CACHE.computeIfAbsent(s, object -> {
            JdbcTable annotation = clazz.getAnnotation(JdbcTable.class);
            String tableName = null;
            if (null != annotation && StrUtil.isNotBlank(annotation.name())) {
                tableName = annotation.name();
            }
            if (StrUtil.isBlank(tableName) && clazz.isAnnotationPresent(Table.class)) {
                Table table = clazz.getAnnotation(Table.class);
                if (Objects.nonNull(table)) {
                    tableName = table.name();
                }
            }
            if (StrUtil.isBlank(tableName) && clazz.isAnnotationPresent(DDLTable.class)) {
                DDLTable annotation2 = clazz.getAnnotation(DDLTable.class);
                tableName = annotation2.tableName();
            }
            return tableName;
        });

    }

    @Override
    public boolean skipColumn(Field field) {
        String s = NA + field.getType().getName() + SP.HASH + field.getName() + SP.HASH + "skipColumn";
        return (boolean) CACHE.computeIfAbsent(s, object -> {
            int modifiers = field.getModifiers();

            if (
                    Modifier.isStatic(modifiers) ||
                            Modifier.isFinal(modifiers) ||
                            Modifier.isTransient(modifiers)
            ) {
                return true;
            }
            return field.isAnnotationPresent(JdbcIgnore.class) || field.isAnnotationPresent(Transient.class);
        });

    }
}
