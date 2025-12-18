package easy4j.module.datasource.dynamic;

import java.lang.annotation.*;

/**
 * 自定义数据源注解
 * 用于动态切换数据源的注解
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {
    // 数据源标识（默认使用配置的默认数据源）
    String value() default "";
}