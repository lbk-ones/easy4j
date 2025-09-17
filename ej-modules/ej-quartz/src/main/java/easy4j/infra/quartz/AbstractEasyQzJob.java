package easy4j.infra.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEasyQzJob implements Job {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract void executeJob(JobExecutionContext context) throws JobExecutionException;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobKey key = jobDetail.getKey();
        log.info("begin execute job....{},{}", key.getName(), key.getGroup());
        long beginTime = System.currentTimeMillis();
        executeJob(context);
        log.info("end job,cost time....{}", (System.currentTimeMillis() - beginTime));

    }
}
