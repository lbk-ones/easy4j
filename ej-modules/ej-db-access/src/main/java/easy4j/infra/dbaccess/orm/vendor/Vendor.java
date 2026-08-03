package easy4j.infra.dbaccess.orm.vendor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ServiceLoaderUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 字段规则调用
 *
 * @author bokun.li
 * @since 2.1.5
 */
public class Vendor {
    public static final List<VendorSpi> vendorList = ServiceLoaderUtils.load(VendorSpi.class);

    static {
        vendorList.sort((o1, o2) -> {
            Integer priority1 = ObjectUtil.defaultIfNull(o1.getPriority(), 1);
            Integer priority2 = ObjectUtil.defaultIfNull(o2.getPriority(), 1);
            return priority1.compareTo(priority2);
        });
    }


    public static boolean isPk(Field field) {
        if (field == null) return false;
        for (VendorSpi vendorSpi : vendorList) {
            if (vendorSpi.isPk(field)) {
                return true;
            }
        }
        return false;
    }

    public static String getColumnName(Field field) {
        if (field == null) return null;
        for (VendorSpi vendorSpi : vendorList) {
            String columnName = vendorSpi.getColumnName(field);
            if (StrUtil.isNotBlank(columnName)) {
                return columnName;
            }
        }
        return field.getName();
    }

    public static boolean isAutoIncrement(Field field) {
        if (field == null) return false;
        for (VendorSpi vendorSpi : vendorList) {
            boolean isAutoIncrement = vendorSpi.isAutoIncrement(field);
            if (isAutoIncrement) {
                return true;
            }
        }
        return false;
    }

    public static String getSchema(Class<?> clazz) {
        if (clazz == null) return null;
        for (VendorSpi vendorSpi : vendorList) {
            String schema = vendorSpi.getSchema(clazz);
            if (StrUtil.isNotBlank(schema)) {
                return schema;
            }
        }
        return null;
    }

    public static String getTableName(Class<?> clazz) {
        if (clazz == null) return null;
        for (VendorSpi vendorSpi : vendorList) {
            String tableName = vendorSpi.getTableName(clazz);
            if (StrUtil.isNotBlank(tableName)) {
                return tableName;
            }
        }
        return clazz.getSimpleName();
    }

    public static boolean skipColumn(Field field) {
        if (field == null) return true;
        for (VendorSpi vendorSpi : vendorList) {
            boolean isSkip = vendorSpi.skipColumn(field);
            if (isSkip) {
                return true;
            }
        }
        return false;
    }
}
