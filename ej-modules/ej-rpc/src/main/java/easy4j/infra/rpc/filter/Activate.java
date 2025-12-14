package easy4j.infra.rpc.filter;

import easy4j.infra.rpc.enums.ExecutorSide;

import java.lang.annotation.*;


@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Activate {

    /**
     * client or server
     * group的优先级高于 easy4j.infra.rpc.filter.RpcFilter#getExecutorSide() 这个回调
     */
    ExecutorSide[] group() default {};

    /**
     * attachment or context contain the name's value
     */
    String[] hasValue() default {};

}
