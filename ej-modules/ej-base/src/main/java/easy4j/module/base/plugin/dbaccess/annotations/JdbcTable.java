package easy4j.module.base.plugin.dbaccess.annotations;

import java.lang.annotation.*;

/**
 * JdbcTable
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JdbcTable {

    // 数据库名
    String schema() default "";

    // 表名
    String name() default "";
}