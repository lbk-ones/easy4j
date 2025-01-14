package easy4j.module.base.annotations;


import java.lang.annotation.*;

@Target({ElementType.FIELD,ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desc {

    String value() default "";
}
