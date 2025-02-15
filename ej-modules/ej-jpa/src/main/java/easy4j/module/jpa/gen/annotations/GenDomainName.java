package easy4j.module.jpa.gen.annotations;

import easy4j.module.base.annotations.Desc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Desc("生成的模块名称 用于绑定Controller注解相关")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface GenDomainName {
    public String value() default "";
}
