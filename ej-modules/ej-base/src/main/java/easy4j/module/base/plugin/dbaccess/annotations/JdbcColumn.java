package easy4j.module.base.plugin.dbaccess.annotations;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JdbcColumn {

    // 列名称
    String name() default "";

    // 列类型
    boolean isPrimaryKey() default false;

    // 主键自动递增
    boolean autoIncrement() default false;
}
