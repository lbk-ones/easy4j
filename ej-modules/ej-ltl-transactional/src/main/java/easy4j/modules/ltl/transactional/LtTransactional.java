package easy4j.modules.ltl.transactional;


import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Transactional
public @interface LtTransactional {


    @AliasFor(
            annotation = Transactional.class,
            attribute = "rollbackFor"
    )
    Class<? extends Throwable>[] rollbackFor() default {};

    @AliasFor(
            annotation = Transactional.class,
            attribute = "propagation"
    )
    Propagation propagation() default Propagation.REQUIRED;


    @AliasFor(
            annotation = Transactional.class,
            attribute = "isolation"
    )
    Isolation isolation() default Isolation.DEFAULT;


    String businessKey() default "";

    String baenName() default "";
    String beanMethod() default "";

    String uniqueKey() default "";

    int retryCount() default 3;
}
