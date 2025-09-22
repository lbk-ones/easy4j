package easy4j.infra.quartz;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import freemarker.template.utility.UnrecognizedTimeZoneException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * job 任务注册
 *
 * @author bokun.li
 * @date 2025-09-17
 */
@Slf4j
public class QuartzJobRegister implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    boolean isInit = false;
    Scheduler scheduler;
    ApplicationContext applicationContext;
    Easy4jQzScheduler easy4jQuartzScheduler;

    public QuartzJobRegister(Scheduler scheduler, Easy4jQzScheduler easy4jQuartzScheduler2) {
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
                    Boolean property = Easy4j.getProperty("spring.quartz.auto-startup", boolean.class);
                    if (!property) {
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
