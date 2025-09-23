package easy4j.infra.quartz.demo;

import easy4j.infra.quartz.AbstractEasyQzJob;
import easy4j.infra.quartz.Easy4jQzJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

// 使用自定义注解标记任务类，配置调度参数
@Easy4jQzJob(
    name = "demoJob",
    group = "DEMO_GROUP",
    cron = "0/10 * * * * ?", // 每10秒执行一次（支持Spring EL：${demo.job.cron}）
    description = "示例定时任务"
)
public class DemoJob extends AbstractEasyQzJob {

    // 自动注入Spring管理的Bean
    @Autowired
    private DemoService demoService;

    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        demoService.doSomething();
        System.out.println("DemoJob执行：" + System.currentTimeMillis());
    }
}