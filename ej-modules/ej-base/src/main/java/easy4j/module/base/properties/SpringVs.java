package easy4j.module.base.properties;


import java.lang.annotation.*;

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
