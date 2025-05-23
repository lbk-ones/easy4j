package easy4j.module.base.module;

import easy4j.module.base.utils.SysConstant;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 根据配置来决定是否启用这个模块 默认是不启用的 默认是false
 *     @Bean
 *     @Moodule("xxx.enable") public XXX xxx() {
 *          return new XXX();
 *     }
 *
 *
 *<br/>
 * 说人话就是 当 easy4j.xxx.enable=true 的时候，才会启用这个模块(才会加载 XXX 这个 bean)
 * <br/>
 * 如果配置成 xxx.enable:true 默认就是开启
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(ModuleCondition.class)
public @interface Module {

    /**
     * 组件名称 不能带前缀
     */
    String[] value();
}
