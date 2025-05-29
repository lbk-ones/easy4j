package easy4j.module.sauth.annotations;

import java.lang.annotation.*;

/**
 * HasCustom
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasCustom {
}