package easy4j.infra.quartz.demo;

import easy4j.infra.common.annotations.Desc;
import easy4j.infra.quartz.Easy4jQzJob;
import org.quartz.JobExecutionContext;

@Desc("这个类可以不纳入spring管理，当然纳入也没有问题")
public class MethodJob {

    @Easy4jQzJob(
            name = "demo1",
            group = "DEMO_GROUP",
            cron = "0/6 * * * * ?",
            description = "demo1方法示例定时任务"
    )
    public void test(JobExecutionContext context) {
        System.out.println("测试方法任务调度");

    }

    @Easy4jQzJob(
            name = "demo2",
            group = "DEMO_GROUP",
            cron = "0/8 * * * * ?",
            description = "demo2方法示例定时任务"
    )
    public void test2(JobExecutionContext context) {

        System.out.println("测试222方法示例定时任务2");

    }

}
