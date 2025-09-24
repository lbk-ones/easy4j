package easy4j.infra.quartz;

import org.quartz.JobDataMap;

import cn.hutool.extra.spring.SpringUtil;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import easy4j.infra.base.properties.cc.ConfigCenterFactory;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.api.lock.DbLock;
import freemarker.template.utility.DateUtil;
import freemarker.template.utility.UnrecognizedTimeZoneException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 扫描@Easy4jQzJob注解，自动注册JobDetail和Trigger的BeanDefinition
 *
 * @author bokun.li
 */
@Slf4j
public class QuartzJobProcessor implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableEasy4jQzJobs.class.getName());
        String[] basePackages = (String[]) annotationAttributes.get("basePackages");

        if (basePackages == null || basePackages.length == 0) {
            // 默认扫描当前类所在包
            String basePackage = ClassUtils.getPackageName(importingClassMetadata.getClassName());
            basePackages = new String[]{basePackage};
        }

        // 扫描并处理带@Easy4jQzJob注解的类
        for (String basePackage : basePackages) {
            scanAndRegisterJobs(basePackage, registry);
        }
    }

    private void scanAndRegisterJobs(String basePackage, BeanDefinitionRegistry registry) {
        // 扫描带@Easy4jQzJob注解的类
        Set<BeanDefinition> candidateComponents = QuartzJobScanner.scan(basePackage);

        for (BeanDefinition beanDefinition : candidateComponents) {
            try {
                // 获取类的全限定名
                String className = beanDefinition.getBeanClassName();
                if (StrUtil.isBlank(className)) {
                    continue;
                }
                Class<?> jobClass = ClassUtils.forName(className, this.getClass().getClassLoader());

                Easy4jQzJob quartzJob = jobClass.getAnnotation(Easy4jQzJob.class);
                if (quartzJob != null) {
                    // 注册JobDetail和Trigger
                    registerJobAndTrigger(jobClass, quartzJob, registry);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to load job class", e);
            }
        }
    }

    /**
     * 注册JobDetail和Trigger的BeanDefinition
     */
    private void registerJobAndTrigger(Class<?> jobClass, Easy4jQzJob quartzJob, BeanDefinitionRegistry registry) {
        boolean isSubclass = Job.class.isAssignableFrom(jobClass) &&
                !jobClass.equals(Job.class);
        if (!isSubclass) {
            log.info(SysLog.compact("skip the class register quartz job" + jobClass.getName()));
            return;
        }
        Class<? extends Job> jobClass2 = (Class<? extends Job>) jobClass;
        String jobName = StringUtils.hasText(quartzJob.name()) ? quartzJob.name() : jobClass.getSimpleName();
        String triggerName = jobName + "Trigger";
        String jobBeanName = jobName + "JobDetail";
        String group = quartzJob.group();
        String triggerBeanName = jobName + "Trigger";
        String cron = quartzJob.cron();
        // cron hot reload
        String s1 = cronHotReload(group, jobName, triggerName);
        if (StrUtil.isNotBlank(s1)) cron = s1;
        BeanDefinition jobDetailDefinition = BeanDefinitionBuilder.genericBeanDefinition(JobDetail.class, () -> getJobDetail(jobClass2, quartzJob)).getBeanDefinition();
        registry.registerBeanDefinition(jobBeanName, jobDetailDefinition);
        String finalCron = cron;
        BeanDefinition triggerDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(Trigger.class, () -> getTrigger(jobClass, quartzJob, finalCron, (e) -> environment.resolvePlaceholders(e)))
                .getBeanDefinition();
        triggerDefinition.setDependsOn(jobBeanName);
        registry.registerBeanDefinition(triggerBeanName, triggerDefinition);
    }

    /**
     * gen
     * @param jobClass
     * @param quartzJob
     * @return
     */
    public static JobDetail getJobDetail(Class<? extends Job> jobClass, Easy4jQzJob quartzJob) {
        String jobName = StringUtils.hasText(quartzJob.name()) ? quartzJob.name() : jobClass.getSimpleName();
        String group = quartzJob.group();
        String description = quartzJob.description();
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobName, group)
                .withDescription(description)
                .storeDurably(true)
                .build();
    }

    /**
     * 根据注解信息和类信息获取触发器
     *
     * @param jobClass    任务类class对象
     * @param quartzJob   注解信息
     * @param finalCron   cron表达式
     * @param cronConvert cron转换函数
     * @return
     */
    public static Trigger getTrigger(Class<?> jobClass, Easy4jQzJob quartzJob, String finalCron, Function<String, String> cronConvert) {
        String jobName = StringUtils.hasText(quartzJob.name()) ? quartzJob.name() : jobClass.getSimpleName();
        String triggerName = jobName + "Trigger";
        String group = quartzJob.group();
        String s = quartzJob.timeZone();
        String description = quartzJob.description();

        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .forJob(JobKey.jobKey(jobName, group)) // 绑定JobDetail
                .withIdentity(triggerName, group);

        // 根据注解配置选择Cron或固定间隔
        if (!StrUtil.isBlank(finalCron)) {
            String cronExpression = cronConvert.apply(finalCron);
            // 解析Cron表达式（支持Spring EL表达式，如${cron.expression}）

            if (!isValidCronExpression(cronExpression)) {
                throw new IllegalArgumentException("the job " + jobName + "'s cron " + cronExpression + " is not valid , please check");
            }
            try {
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionFireAndProceed()
                        .inTimeZone(DateUtil.getTimeZone(s)));
            } catch (UnrecognizedTimeZoneException e) {
                throw new RuntimeException(e);
            }
        } else if (quartzJob.fixedRate() > 0) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMilliseconds(quartzJob.fixedRate())
                    .repeatForever());
        } else {
            throw new IllegalArgumentException("Job " + jobName + " must specify cron or fixedRate");
        }
        triggerBuilder.withDescription(description);
        boolean printLog = quartzJob.printLog();
        String metricCountName = quartzJob.metricCountName();
        String metricTimeName = quartzJob.metricTimeName();
        String metricCountDesc = quartzJob.metricCountDesc();
        String metricTimeDesc = quartzJob.metricTimeDesc();
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(QzConstant.PRINT_LOG, printLog);
        jobDataMap.put(QzConstant.METRIC_COUNT_NAME, metricCountName);
        jobDataMap.put(QzConstant.METRIC_TIME_NAME, metricTimeName);
        jobDataMap.put(QzConstant.METRIC_COUNT_DESC, metricCountDesc);
        jobDataMap.put(QzConstant.METRIC_TIME_DESC, metricTimeDesc);
        triggerBuilder.usingJobData(jobDataMap);
        return triggerBuilder.build();
    }

    private String cronHotReload(String group, String jobName, String triggerName) {
        String s = group + SP.DOT + jobName + SP.DOT;
        // subscribe and get
        return ConfigCenterFactory.get().subscribe(s + "cron", (key, res, type) -> {
            if ("2".equals(type)) {
                String lockKey = "quartz:lock:" + key;
                DbLock dbLock = Easy4j.getContext().get(DbLock.class);
                // if not aquire lock direct exist
                dbLock.lock(lockKey, 5, "cron hot reload lock");
                try {
                    String[] split = ListTs.split(key, SP.DOT);
                    String group2 = split[0];
                    Easy4jQzScheduler scheduler = SpringUtil.getBean(Easy4jQzScheduler.class);
                    scheduler.updateCronTrigger(triggerName, group2, res);
                } catch (SchedulerException e) {
                    throw new RuntimeException(e);
                } finally {
                    dbLock.unLock(lockKey);
                }
            }
        });
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    public static boolean isValidCronExpression(String cronExpression) {
        try {
            new CronTriggerImpl().setCronExpression(cronExpression);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
