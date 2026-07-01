package io.github.lbkones.encryption.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MaskField {

    /**
     * 保留前多少位，默认0
     */
    int prefixLength() default 0;

    /**
     * 保留后多少位，默认0
     */
    int suffixLength() default 0;
}
