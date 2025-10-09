package easy4j.infra.quartz;

import java.lang.annotation.*;

/**
 * 自定义注解，用于标记Quartz任务类，自动注册JobDetail和Trigger
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Easy4jQzJob {

    /**
     * 任务名称（默认使用类名）
     */
    String name() default "";

    /**
     * 任务组名（默认"DEFAULT"）
     */
    String group() default "DEFAULT";

    /**
     * Cron表达式（与fixedRate二选一）
     */
    String cron() default "";

    /**
     * 固定间隔时间（毫秒），与cron二选一
     * 尽量使用 cron表达式
     */
    long fixedRate() default -1;

    /**
     * 任务描述
     */
    String description() default "";


    /**
     * 是否打印日志
     */
    boolean printLog() default true;

    /**
     * Count指标名称
     */
    String metricCountName() default QzConstant.DEFAULT_METRIC_QUARTZ_COUNT_NAME;

    /**
     * Count指标注释
     */
    String metricCountDesc() default "quartz job exe count static";

    /**
     * Time指标名称
     */
    String metricTimeName() default QzConstant.DEFAULT_METRIC_QUARTZ_TIME_NAME;

    /**
     * Time指标注释
     */
    String metricTimeDesc() default "quartz job exe cost time";

    /**
     * 时区 默认上海
     */
    String timeZone() default "Asia/Shanghai";

    /**
     * 重启刷新，一般是刷新cron表达式和周期时间，默认刷新
     */
    boolean restartRefresh() default true;
}
