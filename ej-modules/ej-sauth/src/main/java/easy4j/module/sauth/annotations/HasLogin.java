package easy4j.module.sauth.annotations;

import java.lang.annotation.*;

/**
 * 需要登陆
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasLogin {
}
