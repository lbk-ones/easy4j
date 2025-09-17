package easy4j.infra.quartz;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Easy4jQuartzJob {

    String cronTab() default "";

    String name() default "";

    String group() default "";
}
