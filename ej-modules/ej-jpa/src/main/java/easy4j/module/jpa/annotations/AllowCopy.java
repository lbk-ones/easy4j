package easy4j.module.jpa.annotations;

import easy4j.module.base.annotations.Desc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Desc("目前只用来控制是否更新了")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD})
public @interface AllowCopy {
    String value() default "";
}
