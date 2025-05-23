package easy4j.module.base.plugin.dbaccess.annotations;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JdbcIgnore {

}
