package easy4j.module.base.properties;


import java.lang.annotation.*;

/**
 * SpringVs
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpringVs {

    /**
     * 解释
     * @return
     */
    String desc() default "";

    /**
     * spring对照
     * @return
     */
    String[] vs() default {""};
}