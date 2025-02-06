package easy4j.module.sentinel;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SentinelAutoConfiguration.class)
public @interface EnableFlowDegrade {
}