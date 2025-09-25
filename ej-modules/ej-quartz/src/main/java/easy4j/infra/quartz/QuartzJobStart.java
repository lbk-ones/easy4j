package easy4j.infra.quartz;

import com.google.common.collect.Sets;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.api.lock.DbLock;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

/**
 * job 任务启动
 * 并重新更新 注解类型任务
 *
 * @author bokun.li
 * @date 2025-09-17
 */
@Slf4j
public class QuartzJobStart implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    boolean isInit = false;
    Scheduler scheduler;
    ApplicationContext applicationContext;
    Easy4jQzScheduler easy4jQuartzScheduler;

    public QuartzJobStart(Scheduler scheduler, Easy4jQzScheduler easy4jQuartzScheduler2) {
        this.scheduler = scheduler;
        this.easy4jQuartzScheduler = easy4jQuartzScheduler2;
    }

    public static String getDefaultSysQuartzGroupName() {
        String serverName = Easy4j.getProperty(SysConstant.EASY4J_SERVER_NAME);
        return serverName + "AnnotationQuartzJobGroup";
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext2) throws BeansException {
        this.applicationContext = applicationContext2;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        if (!isInit && applicationContext.getParent() == null) {
            isInit = true;
            try {
                if (scheduler != null) {
                    // reschedule trigger
                    DbLock dbLock = Easy4j.getContext().get(DbLock.class);
                    String lockKey = "easy4j-quartz-start-init-override-lock";
                    try {
                        dbLock.lock(lockKey, 2, "注解任务初始化上锁");
                    } catch (Exception e) {
                        return;
                    }
                    int refreshCount = 0;
                    try {
                        log.info(SysLog.compact("begin scan quartz job class..."));
                        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Easy4jQzJob.class);
                        // pause all
                        scheduler.standby();
                        for (Object value : beansWithAnnotation.values()) {
                            Class<?> aClass1 = value.getClass();
                            boolean isSubclass = Job.class.isAssignableFrom(aClass1);
                            if (!isSubclass) {
                                log.info(SysLog.compact("skip the class register quartz job" + aClass1.getName()));
                                return;
                            }
                            Class<? extends Job> aClass11 = (Class<? extends Job>) aClass1;
                            Easy4jQzJob annotation = aClass1.getAnnotation(Easy4jQzJob.class);
                            if (null != annotation && annotation.restartRefresh()) {
                                refreshCount++;
                                //JobDetail jobDetail = QuartzJobProcessor.getJobDetail(aClass11, annotation);
                                Trigger trigger = QuartzJobProcessor.getTrigger(aClass11, annotation, annotation.cron(), Function.identity());
                                String name = annotation.name();
                                int count = 5;
                                while (count > 0) {
                                    try {
                                        scheduler.rescheduleJob(trigger.getKey(), trigger);
                                        log.info(name + " reschedule success ");
                                        count = -1;
                                    } catch (JobPersistenceException e) {
                                        if (log.isErrorEnabled()) {
                                            log.error(name + " reschedule error: " + e.getClass().getName() + e.getMessage());
                                        }
                                        try {
                                            Thread.sleep(200L);
                                        } catch (InterruptedException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                        count--;
                                    } catch (Exception e) {
                                        count = -1;
                                    }
                                }

                            }
                        }
                    } finally {
                        if (scheduler.isInStandbyMode() && !scheduler.isStarted()) {
                            // resume
                            scheduler.start();
                        }
                        dbLock.unLock(lockKey);
                    }

                    log.info(SysLog.compact("success refresh {} quartz job class", String.valueOf(refreshCount)));
                    Boolean property = Easy4j.getProperty("spring.quartz.auto-startup", boolean.class);
                    if (!property && !scheduler.isStarted()) {
                        log.info(SysLog.compact("starting quartz job ....."));
                        long begin = System.currentTimeMillis();
                        scheduler.start();
                        log.info(SysLog.compact("starting quartz job cost.....{}ms", String.valueOf(System.currentTimeMillis() - begin)));
                    }

                }
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
