package easy4j.module.base.log;

import java.lang.annotation.*;

/**
 * RequestLog
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestLog {
}