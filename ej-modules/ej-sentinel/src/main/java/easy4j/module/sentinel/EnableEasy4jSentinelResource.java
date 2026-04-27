package easy4j.module.sentinel;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 兼容SentinelResource注解
 * 如果不使用sca架构的话 SentinelResource 注解将变得没啥作用
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SentinelAutoConfiguration2.class)
public @interface EnableEasy4jSentinelResource {
}
