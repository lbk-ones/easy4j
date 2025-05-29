package easy4j.module.base.plugin.doc;

import easy4j.module.base.annotations.Desc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ControllerModule
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerModule {
    /**
     * 模块名称
     *
     * @return 模块名称
     */
    @Desc("地址 尽量以英文开头 最好和 @RestController类上面的 @RequestMapping路径保持一致")
    String name() default "";
 
    /**
     * 描述信息
     *
     * @return 描述信息
     */
    @Desc("尽量以中文")
    String description() default "";

}