package easy4j.infra.log.operate;


import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用@EnableEasy4jOperateLog注解可以开启操作日志记录功能
 */
@Import(value = {OperateLogAutoConfiguration.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableEasy4jOperateLog {
}
