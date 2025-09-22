package easy4j.infra.quartz;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

/**
 * 启用Quartz任务自动注册（扫描@QuartzJob注解）
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(QuartzJobProcessor.class) // 导入处理器
public @interface EnableEasy4jQtJobs {

    /**
     * 指定扫描的包路径
     */
    String[] basePackages() default {};
}
