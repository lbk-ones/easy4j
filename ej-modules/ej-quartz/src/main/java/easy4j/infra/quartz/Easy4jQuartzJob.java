package easy4j.infra.quartz;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Easy4jQuartzJob {

    /**
     * 定时任务表达式
     * @return
     */
    String cronTab() default "";

    /**
     * 任务名称默认类的名称
     * @return
     */
    String name() default "";

    /**
     * 任务组
     * @return
     */
    String group() default "";

    /**
     * 是否打印日志
     * @return
     */
    boolean printLog() default true;

    /**
     * Count指标名称
     * @return
     */
    String metricCountName() default QzConstant.DEFAULT_METRIC_QUARTZ_COUNT_NAME;

    /**
     * Count指标注释
     * @return
     */
    String metricCountDesc() default "quartz job exe count static";

    /**
     * Time指标名称
     * @return
     */
    String metricTimeName() default QzConstant.DEFAULT_METRIC_QUARTZ_TIME_NAME;

    /**
     * Time指标注释
     * @return
     */
    String metricTimeDesc() default "quartz job exe cost time";
}
