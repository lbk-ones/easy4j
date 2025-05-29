package easy4j.module.base.plugin.dbaccess.annotations;

import java.lang.annotation.*;

/**
 * JdbcIgnore
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JdbcIgnore {

}