package easy4j.module.sauth.annotations;

import java.lang.annotation.*;

/**
 * 是否需要权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasAuthority {
}
