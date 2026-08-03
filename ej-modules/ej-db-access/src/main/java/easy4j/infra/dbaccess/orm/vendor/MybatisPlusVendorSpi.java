package easy4j.infra.dbaccess.orm.vendor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;

import java.lang.reflect.Field;
import java.util.Objects;

public class MybatisPlusVendorSpi extends AbsVendorSpi {

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
        String rn = null;
        if (field.isAnnotationPresent(TableField.class)) {
            rn = field.getAnnotation(TableField.class).value();
        }
        return rn;
    }

    @Override
    public boolean isAutoIncrement(Field field) {
        return field.isAnnotationPresent(TableId.class) && field.getAnnotation(TableId.class).type() == IdType.AUTO;
    }

    @Override
    public String getSchema(Class<?> clazz) {
        if (clazz == null) return "";
        TableName annotation1 = clazz.getAnnotation(TableName.class);
        if (Objects.nonNull(annotation1)) {
            return annotation1.schema();
        }
        return null;
    }

    @Override
    public String getTableName(Class<?> clazz) {
        if (clazz == null) return null;
        String tableName = null;
        TableName annotation1 = clazz.getAnnotation(TableName.class);
        if (Objects.nonNull(annotation1)) {
            tableName = annotation1.value();
        }
        return tableName;
    }

    @Override
    public boolean skipColumn(Field field) {
        boolean skip = false;
        if (field.isAnnotationPresent(TableField.class)) {
            TableField annotation = field.getAnnotation(TableField.class);
            if (!annotation.exist()) {
                skip = true;
            }
        }
        return skip;
    }
}
