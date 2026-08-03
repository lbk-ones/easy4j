package easy4j.infra.dbaccess.orm.vendor;

import java.lang.reflect.Field;

/**
 * 扩展字段规则
 *
 * @author bokun.li
 * @since 2.1.5
 */
public interface VendorSpi {

    /**
     * 规则优先级
     * @return
     */
    Integer getPriority();
    /**
     * 是否主键
     * @return boolean
     */
    boolean isPk(Field field);
    /**
     * 获取字段名称
     * @return boolean
     */
    String getColumnName(Field field);
    /**
     * 是否递增
     * @return boolean
     */
    boolean isAutoIncrement(Field field);
    /**
     * 获取schema名称
     * @return String
     */
    String getSchema(Class<?> clazz);
    /**
     * 获取表名称,如果都没有，则最后会降级为，类名称
     * @return String
     */
    String getTableName(Class<?> clazz);
    /**
     * 是否跳过该字段不做处理
     * @return boolean
     */
    boolean skipColumn(Field field);

}
