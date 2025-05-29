package easy4j.module.sentinel;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableFlowDegrade
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SentinelAutoConfiguration.class)
public @interface EnableFlowDegrade {
}