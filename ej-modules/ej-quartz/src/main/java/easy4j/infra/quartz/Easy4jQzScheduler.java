package easy4j.infra.quartz;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import easy4j.infra.common.utils.SysLog;
import freemarker.template.utility.DateUtil;
import freemarker.template.utility.UnrecognizedTimeZoneException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.DisposableBean;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作quartz的工具类
 *
 * @author bokun.li
 * @date 2025-09-17
 */
@Getter
@Slf4j
public final class Easy4jQzScheduler implements DisposableBean {

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
        JobDataMap jobDataMap = ObjectUtil.defaultIfNull(jobInfo.getJobDataMap(), new JobDataMap());
        if (StrUtil.isBlank(jobName) || StrUtil.isBlank(cronTab)) {
            log.info("skip the jobName{},{}", jobName, cronTab);
            return;
        }
        Date now = new Date();
        if (startDate == null || startDate.before(now) || jobClass == null) {
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
                                .withMisfireHandlingInstructionFireAndProceed()
                                .inTimeZone(DateUtil.getTimeZone(timeZone)))
                .build();
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .setJobData(jobDataMap)
                .storeDurably(true)
                .build();
        //subscribe to schedule
        scheduler.scheduleJob(jobDetail, Sets.newHashSet(cronTrigger), true);
    }

    /**
     * 调度一个临时任务（即只执行一次）
     *
     * @param jobInfo
     * @throws SchedulerException
     */
    public void scheduleTempJob(JobInfo jobInfo) throws SchedulerException {
        String jobName = jobInfo.getJobName();
        String jobGroup = jobInfo.getJobGroup();
        Class<? extends Job> jobClass = jobInfo.getJobClass();
        JobDataMap jobDataMap = jobInfo.getJobDataMap();
        if (StrUtil.isBlank(jobName) || StrUtil.isBlank(jobGroup) || jobClass == null) {
            log.info("skip the jobName{}", jobName);
            return;
        }
        JobKey jobKey = new JobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup());
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startNow()
                .build();
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .setJobData(jobDataMap)
                .storeDurably(false)
                .build();
        //subscribe to schedule
        scheduler.scheduleJob(jobDetail, trigger);
    }


    /**
     * 任务是否存在
     *
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    public boolean checkJobExists(JobKey jobKey) throws SchedulerException {
        return scheduler.checkExists(jobKey);
    }

    /**
     * 触发器是否存在
     *
     * @param triggerKey
     * @return
     * @throws SchedulerException
     */
    public boolean checkTriggerExists(TriggerKey triggerKey) throws SchedulerException {
        return scheduler.checkExists(triggerKey);
    }

    /**
     * 立即开始一个任务
     *
     * @param jobKey
     * @throws SchedulerException
     */
    public void startJobNow(JobKey jobKey) throws SchedulerException {
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
     * @param jobKey 任务key
     * @param map    传递到任务的参数
     * @throws SchedulerException
     */
    public void startJob(JobKey jobKey, JobDataMap map) throws SchedulerException {
        this.scheduler.triggerJob(jobKey, map);
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
     * 暂停一个任务
     *
     * @param name  任务名称，通常是任务定义ID
     * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
     * @throws SchedulerException
     */
    public void pauseJob(String name, String group) throws SchedulerException {
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
        this.scheduler.resumeJob(new JobKey(name, group));
    }


    /**
     * 验证cron表达式是否有效
     *
     * @param cronExpression cron表达式
     * @return 是否有效
     */
    public boolean isValidCronExpression(String cronExpression) {
        try {
            new CronTriggerImpl().setCronExpression(cronExpression);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


    /**
     * 停止并移除指定任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务组
     * @return 是否成功移除
     * @throws SchedulerException 调度器异常
     */
    public boolean stopAndRemoveJob(String jobName, String jobGroup) throws SchedulerException {

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

        if (!scheduler.checkExists(jobKey)) {
            return false;
        }

        // 暂停触发器
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        scheduler.pauseTrigger(triggerKey);

        // 移除触发器
        scheduler.unscheduleJob(triggerKey);

        // 删除任务
        scheduler.deleteJob(jobKey);
        return true;
    }

    /**
     * 获取当前正在调度的任务列表
     *
     * @return 任务信息列表
     * @throws SchedulerException 调度器异常
     */
    public List<JobInfo> getRunningJobs() throws SchedulerException {
        List<JobExecutionContext> runningJobs = scheduler.getCurrentlyExecutingJobs();
        return runningJobs.stream().map(context -> {
            JobInfo info = new JobInfo();
            JobKey jobKey = context.getJobDetail().getKey();
            info.setJobName(jobKey.getName());
            info.setJobGroup(jobKey.getGroup());
            info.setJobClass(context.getJobDetail().getJobClass());
            info.setStatus(JobStatus.RUNNING);
            info.setStartDate(context.getFireTime());
            info.setNextState(context.getNextFireTime());
            info.setLastState(context.getPreviousFireTime());

            Trigger trigger = context.getTrigger();
            if (trigger instanceof CronTrigger) {
                info.setCronTab(((CronTrigger) trigger).getCronExpression());
            }
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * 获取所有任务列表（包括暂停的）
     *
     * @return 所有任务信息
     * @throws SchedulerException 调度器异常
     */
    public List<JobInfo> getAllJobs() throws SchedulerException {
        List<JobInfo> jobInfos = new ArrayList<>();

        // 获取所有任务组
        List<String> jobGroups = scheduler.getJobGroupNames();

        for (String group : jobGroups) {
            // 获取组内所有任务
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));

            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                for (Trigger trigger : triggers) {
                    JobInfo info = new JobInfo();
                    info.setJobName(jobKey.getName());
                    info.setJobGroup(jobKey.getGroup());
                    TriggerKey key = trigger.getKey();
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(key);
                    info.setJobClass(jobDetail.getJobClass());
                    info.setStatus(getTriggerStatus(triggerState));
                    info.setStartDate(trigger.getStartTime());
                    info.setNextState(trigger.getNextFireTime());
                    info.setLastState(trigger.getPreviousFireTime());
                    if (trigger instanceof CronTrigger) {
                        info.setCronTab(((CronTrigger) trigger).getCronExpression());
                    }
                    jobInfos.add(info);
                }
            }
        }

        return jobInfos;
    }


    /**
     * 关闭调度器
     *
     * @throws SchedulerException 调度器异常
     */
    private void shutdown() throws SchedulerException {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown(true);
            log.info(SysLog.compact("the quartz schedule is shutdown !"));
        }
    }


    /**
     * 转换触发器状态为自定义状态枚举
     */
    private static JobStatus getTriggerStatus(Trigger.TriggerState state) {
        switch (state) {
            case NORMAL:
                return JobStatus.NORMAL;
            case PAUSED:
                return JobStatus.PAUSED;
            case COMPLETE:
                return JobStatus.COMPLETE;
            case ERROR:
                return JobStatus.ERROR;
            case BLOCKED:
                return JobStatus.BLOCKED;
            default:
                return JobStatus.UNKNOWN;
        }
    }


    /**
     * 任务状态枚举
     */
    public enum JobStatus {
        NORMAL, PAUSED, RUNNING, COMPLETE, ERROR, BLOCKED, UNKNOWN
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }


    /**
     * 热更新Cron表达式
     * 如果是普通类型（固定频率）的任务，那么也会更新成cron任务
     *
     * @param triggerName       触发器名称
     * @param triggerGroup      触发器组名
     * @param newCronExpression 新的cron表达式
     */
    public void updateCronTrigger(String triggerName, String triggerGroup, String newCronExpression) throws SchedulerException {

        // 1. 获取旧的触发器
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        if (trigger == null) return;
        CronTrigger oldTrigger = null;
        // 2. 如果cron表达式没有变化，无需更新
        String oldCron = null;
        TimeZone timeZone = null;
        try {
            timeZone = DateUtil.getTimeZone("Asia/Shanghai");
        } catch (UnrecognizedTimeZoneException ignored) {

        }
        if (trigger instanceof CronTrigger) {
            oldTrigger = ((CronTrigger) trigger);
            oldCron = oldTrigger.getCronExpression();
            timeZone = oldTrigger.getTimeZone();
        }

        if (StrUtil.isBlank(newCronExpression) || !isValidCronExpression(newCronExpression) || (oldCron != null && oldCron.equals(newCronExpression))) {
            return;
        }

        // 创建新的触发器（使用新的cron表达式）
        CronTrigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey) // 保持原触发器的标识
                .forJob(trigger.getJobKey())
                .withDescription(trigger.getDescription())
                .withSchedule(CronScheduleBuilder.cronSchedule(newCronExpression)
                        .withMisfireHandlingInstructionFireAndProceed()
                        .inTimeZone(timeZone)
                )
                .usingJobData(trigger.getJobDataMap())
                .build();

        // 4. 替换旧的触发器（实现热更新）
        Date date = scheduler.rescheduleJob(triggerKey, newTrigger);
        if (date != null) {
            System.out.println("Cron表达式更新成功：" + newCronExpression);
        }
    }
}
