# quartz 封装

## 1、quartz默认参数封装（quartz参数有些复杂，封装之后可以不用关注于参数，主要聚焦于业务实现）

## 2、封装操作quartz的工具 easy4j.infra.quartz.Easy4jQzScheduler
可以直接注入使用

```text
@Autowired
Easy4jQzScheduler easy4jQzScheduler;

/**
 * 任务调度,如果调度过会替换原来的任务
 *
 * @param jobInfo 任务信息
 * @throws SchedulerException
 * @throws UnrecognizedTimeZoneException
 */
public void scheduleJob(JobInfo jobInfo) throws SchedulerException, UnrecognizedTimeZoneException;
    
/**
 * 调度一个临时任务（即只执行一次）
 *
 * @param jobInfo
 * @throws SchedulerException
 */
public void scheduleTempJob(JobInfo jobInfo) throws SchedulerException;
/**
 * 任务是否存在
 * @param jobKey
 * @return
 * @throws SchedulerException
 */
public boolean checkJobExists(JobKey jobKey) throws SchedulerException;
/**
 * 触发器是否存在
 * @param triggerKey
 * @return
 * @throws SchedulerException
 */
public boolean checkTriggerExists(TriggerKey triggerKey) throws SchedulerException;
/**
 * 立即开始一个任务
 *
 * @param jobKey
 * @throws SchedulerException
 */
public void startJobNow(JobKey jobKey) throws SchedulerException;
/**
 * 立即开始一个任务
 *
 * @param name  任务名称，通常是任务定义ID
 * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
 * @param map   传递到任务的参数
 * @throws SchedulerException
 */
public void startJob(String name, String group, JobDataMap map) throws SchedulerException;

/**
 * 立即开始一个任务
 *
 * @param jobKey 任务key
 * @param map   传递到任务的参数
 * @throws SchedulerException
 */
public void startJob(JobKey jobKey, JobDataMap map) throws SchedulerException;

/**
 * 立即开始一个任务
 *
 * @param name  任务名称，通常是任务定义ID
 * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
 * @throws SchedulerException
 */
public void startJob(String name, String group) throws SchedulerException;
/**
 * 删除一个任务
 *
 * @param jobKey
 * @throws SchedulerException
 */
public void deleteJob(JobKey jobKey) throws SchedulerException;

/**
 * 删除一个任务
 *
 * @param name  任务名称，通常是任务定义ID
 * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
 * @throws SchedulerException
 */
public void deleteJob(String name, String group) throws SchedulerException;


/**
 * 停止一个任务
 *
 * @param name  任务名称，通常是任务定义ID
 * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
 * @throws SchedulerException
 */
public void stopJob(String name, String group) throws SchedulerException;

/**
 * 恢复一个任务
 *
 * @param name  任务名称，通常是任务定义ID
 * @param group 任务组，通常是业务分组，或者项目分组，或者是服务分组
 * @throws SchedulerException
 */
public void resumeJob(String name, String group) throws SchedulerException;
/**
 * 验证cron表达式是否有效
 *
 * @param cronExpression cron表达式
 * @return 是否有效
 */
public boolean isValidCronExpression(String cronExpression);


/**
 * 停止并移除指定任务
 *
 * @param jobName  任务名称
 * @param jobGroup 任务组
 * @return 是否成功移除
 * @throws SchedulerException 调度器异常
 */
public boolean stopAndRemoveJob(String jobName, String jobGroup) throws SchedulerException;


 /**
 * 获取当前正在调度的任务列表
 *
 * @return 任务信息列表
 * @throws SchedulerException 调度器异常
 */
public List<JobInfo> getRunningJobs() throws SchedulerException;


/**
 * 获取所有任务列表（包括暂停的）
 *
 * @return 所有任务信息
 * @throws SchedulerException 调度器异常
 */
public List<JobInfo> getAllJobs() throws SchedulerException;

/**
 * 关闭调度器
 *
 * @throws SchedulerException 调度器异常
 */
private void shutdown() throws SchedulerException;
    
   


```


## 3、封装quartz调度兼容启动

## 4、封装quartz的注解使用方法，同时交给spring ioc容器维护，简单使用如下：

```text
功能启动 启动类添加注解 @EnableEasy4jQzJobs
--------------------------------------------------------------

@Easy4JStarter
@EnableEasy4jQzJobs
public class DataMetaApp {
    public static void main(String[] args) {
        SpringApplication.run(DataMetaApp.class, args);
    }
}
--------------------------------------------------------------
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
--------------------------------------------------------------
package easy4j.infra.quartz.demo;

import org.springframework.stereotype.Service;

@Service
public class DemoService {
    public void doSomething() {
        System.out.println("DemoService执行业务逻辑");
    }
}

```
