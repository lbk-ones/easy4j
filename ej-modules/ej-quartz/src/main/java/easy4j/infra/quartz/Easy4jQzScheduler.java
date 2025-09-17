package easy4j.infra.quartz;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import freemarker.template.utility.DateUtil;
import freemarker.template.utility.UnrecognizedTimeZoneException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.util.Date;

/**
 * 操作quartz的工具类
 *
 * @author bokun.li
 * @date 2025-09-17
 */
@Getter
@Slf4j
public class Easy4jQzScheduler {

    private final Scheduler scheduler;

    public Easy4jQzScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    /**
     * 开始调度
     *
     * @throws SchedulerException
     */
    public void start() throws SchedulerException {
        if (!scheduler.isStarted()) {
            scheduler.start();
        }
    }


    /**
     * 任务调度,如果调度过会替换原来的任务
     *
     * @param jobInfo 任务信息
     * @throws SchedulerException
     * @throws UnrecognizedTimeZoneException
     */
    public void scheduleJob(JobInfo jobInfo) throws SchedulerException, UnrecognizedTimeZoneException {
        Date startDate = jobInfo.getStartDate();
        Date endDate = jobInfo.getEndDate();
        String jobName = jobInfo.getJobName();
        String jobGroup = jobInfo.getJobGroup();
        String cronTab = jobInfo.getCronTab();
        String timeZone = jobInfo.getTimeZone();
        Class<? extends Job> jobClass = jobInfo.getJobClass();
        JobDataMap jobDataMap = jobInfo.getJobDataMap();
        if (StrUtil.isBlank(jobName) || StrUtil.isBlank(cronTab)) {
            log.info("skip the jobName{},{}", jobName, cronTab);
            return;
        }
        Date now = new Date();
        if (startDate == null || startDate.before(now)) {
            startDate = now;
        }
        JobKey jobKey = new JobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup());
        CronTrigger cronTrigger;
        cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(startDate)
                .endAt(endDate)
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(cronTab)
                                .withMisfireHandlingInstructionIgnoreMisfires()
                                .inTimeZone(DateUtil.getTimeZone(timeZone)))
                .build();
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .setJobData(jobDataMap)
                .build();
        //register to schedule
        scheduler.scheduleJob(jobDetail, Sets.newHashSet(cronTrigger), true);
    }

    /**
     * 立即开始一个任务
     *
     * @param jobKey
     * @throws SchedulerException
     */
    public void startJob(JobKey jobKey) throws SchedulerException {
        this.scheduler.triggerJob(jobKey);
    }

    /**
     * 立即开始一个任务
     *
     * @param name  任务名称，通常是任务定义ID
     * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
     * @param map   传递到任务的参数
     * @throws SchedulerException
     */
    public void startJob(String name, String group, JobDataMap map) throws SchedulerException {
        this.scheduler.triggerJob(new JobKey(name, group), map);
    }

    /**
     * 立即开始一个任务
     *
     * @param name  任务名称，通常是任务定义ID
     * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
     * @throws SchedulerException
     */
    public void startJob(String name, String group) throws SchedulerException {
        this.scheduler.triggerJob(new JobKey(name, group));
    }

    /**
     * 删除一个任务
     *
     * @param jobKey
     * @throws SchedulerException
     */
    public void deleteJob(JobKey jobKey) throws SchedulerException {
        this.scheduler.deleteJob(jobKey);
    }

    /**
     * 删除一个任务
     *
     * @param name  任务名称，通常是任务定义ID
     * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
     * @throws SchedulerException
     */
    public void deleteJob(String name, String group) throws SchedulerException {
        this.scheduler.deleteJob(new JobKey(name, group));
    }

    /**
     * 停止一个任务
     *
     * @param name  任务名称，通常是任务定义ID
     * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
     * @throws SchedulerException
     */
    public void stopJob(String name, String group) throws SchedulerException {
        this.scheduler.pauseJob(new JobKey(name, group));
    }

    /**
     * 恢复一个任务
     *
     * @param name  任务名称，通常是任务定义ID
     * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
     * @throws SchedulerException
     */
    public void resumeJob(String name, String group) throws SchedulerException {
        this.scheduler.pauseJob(new JobKey(name, group));
    }


}
