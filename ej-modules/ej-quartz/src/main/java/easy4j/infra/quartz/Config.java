package easy4j.infra.quartz;

import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = {QuartzAutoConfiguration.class})
@ConditionalOnClass(value = Scheduler.class)
public class Config {

    @Bean
    @ConditionalOnMissingBean
    public Easy4jQzScheduler schedulerApi(Scheduler scheduler) {
        return new Easy4jQzScheduler(scheduler);
    }


    @Bean
    public QuartzJobRegister quartzRunner(Scheduler scheduler) {
        return new QuartzJobRegister(scheduler, schedulerApi(scheduler));
    }

}
