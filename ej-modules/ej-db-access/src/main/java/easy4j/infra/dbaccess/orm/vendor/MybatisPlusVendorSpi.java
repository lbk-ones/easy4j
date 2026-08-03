package easy4j.infra.dbaccess.orm.vendor;

import cn.hutool.core.map.reference.WeakKeyConcurrentMap;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import easy4j.infra.common.utils.SP;
import jakarta.persistence.*;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Function;

public class MybatisPlusVendorSpi extends AbsVendorSpi {
    private static final String NA = "mp-";

    private static final WeakKeyConcurrentMap<String, Object> CACHE = new WeakKeyConcurrentMap<>();

    @Override
    public Integer getPriority() {
        return Integer.MIN_VALUE + 1;
    }

    @Override
    public boolean isPk(Field field) {
        return field.isAnnotationPresent(TableId.class);
    }

    @Override
    public String getColumnName(Field field) {
        String s = NA + field.getType().getName() + SP.HASH + field.getName() + SP.HASH + "getColumnName";
        return (String) CACHE.computeIfAbsent(s, s1 -> {
            String rn = null;
            if (field.isAnnotationPresent(TableField.class)) {
                rn = field.getAnnotation(TableField.class).value();
            }
            return rn;
        });

    }

    @Override
    public boolean isAutoIncrement(Field field) {
        String s = NA + field.getType().getName() + SP.HASH + field.getName() + SP.HASH + "isAutoIncrement";

        return (boolean) CACHE.computeIfAbsent(s, s1 -> {
            return field.isAnnotationPresent(TableId.class) && field.getAnnotation(TableId.class).type() == IdType.AUTO;
        });

    }

    @Override
    public String getSchema(Class<?> clazz) {
        String s = NA + clazz.getName() + SP.HASH + "getSchema";
        return (String) CACHE.computeIfAbsent(s, s1 -> {
            TableName annotation1 = clazz.getAnnotation(TableName.class);
            if (Objects.nonNull(annotation1)) {
                return annotation1.schema();
            }
            return "";
        });

    }

    @Override
    public String getTableName(Class<?> clazz) {
        String s = NA + clazz.getName() + SP.HASH + "getTableName";
        return (String) CACHE.computeIfAbsent(s, s1 -> {
            String tableName = "";
            TableName annotation1 = clazz.getAnnotation(TableName.class);
            if (Objects.nonNull(annotation1)) {
                tableName = annotation1.value();
            }
            return tableName;
        });

    }

    @Override
    public boolean skipColumn(Field field) {
        String s = NA + field.getType().getName() + SP.HASH + field.getName() + SP.HASH + "skipColumn";
        return (boolean) CACHE.computeIfAbsent(s, s1 -> {
            boolean skip = false;
            if (field.isAnnotationPresent(TableField.class)) {
                TableField annotation = field.getAnnotation(TableField.class);
                if (!annotation.exist()) {
                    skip = true;
                }
            }
            return skip;
        });

    }
}
