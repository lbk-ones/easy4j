package easy4j.infra.common.utils.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMap {


    /**
     * 必须完全匹配
     * @return
     */
    String url() default "";


    MethodType method() default MethodType.POST;



}
