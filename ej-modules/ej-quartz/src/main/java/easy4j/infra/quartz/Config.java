package easy4j.infra.quartz;

import org.quartz.Scheduler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;
@AutoConfiguration(after = {QuartzAutoConfiguration.class})
@ConditionalOnClass(value = Scheduler.class)
public class Config implements InitializingBean {

    @Bean
    @ConditionalOnMissingBean
    public Easy4jQzScheduler schedulerApi(Scheduler scheduler) {
        return new Easy4jQzScheduler(scheduler);
    }


    @Bean
    public QuartzJobStart quartzRunner(Scheduler scheduler) {
        return new QuartzJobStart(scheduler, schedulerApi(scheduler));
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
