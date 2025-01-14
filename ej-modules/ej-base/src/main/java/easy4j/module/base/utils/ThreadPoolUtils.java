package easy4j.module.base.utils;


import lombok.extern.slf4j.Slf4j;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 多线程池工具
 */
@Slf4j
public class ThreadPoolUtils {

    public static Map<String, ThreadPoolTaskExecutor> threadPoolTaskExecutorMap = new HashMap<>();

    public static ThreadPoolTaskExecutor getThreadPoolTaskExecutor(String poolName) {
        return threadPoolTaskExecutorMap.computeIfAbsent(poolName, ThreadPoolUtils::createThreadPoolTaskExecutor);
    }

    public static ThreadPoolTaskExecutor createThreadPoolTaskExecutor(String poolName) {
        log.info("初始化名称为：{}的线程池", poolName);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数目
        executor.setCorePoolSize(8);
        //指定最大线程数
        executor.setMaxPoolSize(16);
        //队列中最大的数目，默认Integer.MAX_VALUE(2147483647)
        executor.setQueueCapacity(500);
        //线程名称前缀
        executor.setThreadNamePrefix("Thread-" + poolName + "-");
        //rejection-policy：当pool已经达到maxsize的时候，如何处理新任务
        //CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        //对拒绝task的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //线程空闲后的最大存活时间
        executor.setKeepAliveSeconds(10);
        //加载
        executor.initialize();
        return executor;
    }
}
