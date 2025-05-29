package easy4j.module.base.annotations;


import java.lang.annotation.*;

/**
 * Desc
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desc {

    String value() default "";
}