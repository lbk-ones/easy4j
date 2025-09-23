package easy4j.infra.quartz;

import com.github.xiaoymin.knife4j.core.util.StrUtil;
import easy4j.infra.common.utils.SysLog;
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
        // 1. 生成JobDetail的Bean名称
        String jobName = StringUtils.hasText(quartzJob.name()) ? quartzJob.name() : jobClass.getSimpleName();
        String jobBeanName = jobName + "JobDetail";
        String group = quartzJob.group();

        // 2. 注册JobDetail的BeanDefinition
        BeanDefinition jobDetailDefinition = BeanDefinitionBuilder.genericBeanDefinition(JobDetail.class, () ->
                JobBuilder.newJob(jobClass2)
                        .withIdentity(jobName, group)
                        .withDescription(quartzJob.description())
                        .build()
        ).getBeanDefinition();
        registry.registerBeanDefinition(jobBeanName, jobDetailDefinition);

        // 3. 生成Trigger的Bean名称
        String triggerBeanName = jobName + "Trigger";
        String cron = quartzJob.cron();
        String s = quartzJob.timeZone();
        // 4. 注册Trigger的BeanDefinition（依赖JobDetail）
        BeanDefinition triggerDefinition = BeanDefinitionBuilder.genericBeanDefinition(Trigger.class, () -> {
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                    .forJob(JobKey.jobKey(jobName, group)) // 绑定JobDetail
                    .withIdentity(jobName + "Trigger", group);

            // 根据注解配置选择Cron或固定间隔
            if (StringUtils.hasText(cron)) {
                // 解析Cron表达式（支持Spring EL表达式，如${cron.expression}）
                String cronExpression = environment.resolvePlaceholders(cron);
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
            triggerBuilder.forJob(jobName, group);
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
        }).getBeanDefinition();
        registry.registerBeanDefinition(triggerBeanName, triggerDefinition);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    public boolean isValidCronExpression(String cronExpression) {
        try {
            new CronTriggerImpl().setCronExpression(cronExpression);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
