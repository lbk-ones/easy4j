package easy4j.infra.dbaccess.orm.plugin;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface LogicDelete {

    // 注解不能放Object所以如果是数字则改为true
    boolean isNumber() default false;

    // 删除的值
    String yes() default "1";

    // 未删除的值
    String no() default "0";

}
