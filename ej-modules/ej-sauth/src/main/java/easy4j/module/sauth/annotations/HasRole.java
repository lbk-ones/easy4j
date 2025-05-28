package easy4j.module.sauth.annotations;

import java.lang.annotation.*;

/**
 * 需要有某个角色
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasRole {
}
