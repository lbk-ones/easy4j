package easy4j.infra.quartz;

import easy4j.infra.actuator.MetricsBuilder;
import easy4j.infra.actuator.MetricsFactory;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.concurrent.TimeUnit;

/**
 * job继承父类
 * 会自动统计指标
 * 打印执行时间和执行信息
 *
 * @author bokun.li
 * @date 2025/9/18
 */
public abstract class AbstractEasyQzJob extends QuartzJobBean implements Job {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract void executeJob(JobExecutionContext context) throws JobExecutionException;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        long beginTime = System.currentTimeMillis();
        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
        boolean printLog = !mergedJobDataMap.containsKey(QzConstant.PRINT_LOG) || mergedJobDataMap.getBoolean(QzConstant.PRINT_LOG);

        Boolean property = Easy4j.getProperty(SysConstant.EASY4J_IS_GLOBAL_PRINT_LOG, boolean.class);
        if (!property) printLog = false;
        String metricCountName = StringUtils.defaultIfBlank(mergedJobDataMap.getString(QzConstant.METRIC_COUNT_NAME), QzConstant.DEFAULT_METRIC_QUARTZ_COUNT_NAME);
        String metricTimeName = StringUtils.defaultIfBlank(mergedJobDataMap.getString(QzConstant.METRIC_TIME_NAME), QzConstant.DEFAULT_METRIC_QUARTZ_TIME_NAME);
        String metricCountDesc = mergedJobDataMap.getString(QzConstant.METRIC_COUNT_DESC);
        String metricTimeDesc = mergedJobDataMap.getString(QzConstant.METRIC_TIME_DESC);
        MetricsBuilder instance = MetricsFactory.getInstance();
        Counter counter = instance.buildCounter(metricCountName, metricCountDesc, null);
        Counter errorCounter = instance.buildCounter(QzConstant.DEFAULT_METRIC_QUARTZ_EXCEPTION_COUNT_NAME, "quartz exe error count", null);
        counter.increment();
        JobDetail jobDetail = context.getJobDetail();
        JobKey key = jobDetail.getKey();
        if (printLog) {
            log.info("begin execute job....{},{}", key.getName(), key.getGroup());
        }
        Timer timer = instance.buildTimer(metricTimeName, metricTimeDesc, null);
        try {
            executeJob(context);
        } catch (Throwable e) {
            errorCounter.increment();
            throw e;
        }finally {
            long l = System.currentTimeMillis() - beginTime;
            timer.record(l, TimeUnit.MILLISECONDS);
            if (printLog) {
                log.info("end job,cost time....{}ms", l);
            }
        }
    }
}
