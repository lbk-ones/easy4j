package easy4j.module.sauth.annotations;

import java.lang.annotation.*;

/**
 * 开放api
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenApi {

    String mode() default "Api-key";
}
