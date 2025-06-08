/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.common.utils;


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

    public static ThreadPoolTaskExecutor getThreadPoolTaskExecutor(String poolName, int corePoolSize, int maxPoolSize, int queueCapacity) {
        return threadPoolTaskExecutorMap.computeIfAbsent(poolName, e -> {
            return createThreadPoolTaskExecutorBy(e, corePoolSize, maxPoolSize, queueCapacity);
        });
    }

    public static ThreadPoolTaskExecutor createThreadPoolTaskExecutor(String poolName) {
        return createThreadPoolTaskExecutorBy(poolName, 8, 16, 500);
    }

    public static ThreadPoolTaskExecutor createThreadPoolTaskExecutorBy(String poolName, int corePoolSize, int maxPoolSize, int queueCapacity) {
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
