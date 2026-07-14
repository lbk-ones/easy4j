package io.github.lbkones.nacos.pure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 不升级 sprirng cloud 只升级nacos的方案
 *
 * @author bokun.li
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(NacosCustomAutoConfiguration.class)
public @interface EnableNacosConfigCenter {

    /**
     * 应用环境dataId后缀前缀
     */
    String dataIdPrefix() default "";

    /**
     * 应用环境dataId后缀
     */
    String dataIdSuffix() default "";

}
