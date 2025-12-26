package easy4j.infra.common.utils.servletmvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface URequestParam {

    String value() default "";


}
