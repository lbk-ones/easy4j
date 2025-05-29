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
package easy4j.module.jaeger;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.ttl.threadpool.TtlExecutors;
import easy4j.module.jaeger.opentracing.concurrent.TracedExecutorService;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 线程工具类
 *
 * @author bokun.li
 * @date 2022/11/1
 */
@Component
public class ThreadPoolUtils {
    public static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtils.class);
    private final ExecutorService executorService;

    @Autowired
    Tracer tracer;

    public ThreadPoolUtils() {
        int threads = Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threads * 20 + 1,
                Integer.MAX_VALUE, 60L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(threads * 200),
                new ThreadFactory() {
                    long threadNum = 0L;

                    @Override
                    public Thread newThread(Runnable r) {
                        threadNum++;
                        //LoggerUtil.info(this.getClass(),"*** thread pool util begin create thread ***"+threadNum);
                        return new Thread(r, "sys-thread-pool-util-get-" + threadNum);
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());


        this.executorService = TtlExecutors.getTtlExecutorService(new TracedExecutorService(executor, tracer));
        /*
            ScheduledExecutorService monitorExecutor = Executors.newScheduledThreadPool(1);
            monitorExecutor.scheduleAtFixedRate(() -> {
                logger.info("线程池监控 - " + DateUtil.now() +
                        "  线程池大小: " + executorService.getPoolSize()+
                        "  活动线程数: " + executorService.getActiveCount()+
                        "  已完成任务数: " + executorService.getCompletedTaskCount()+
                        "  任务队列大小: " + executorService.getQueue().size());
            }, 0, 60*10, TimeUnit.SECONDS);
        */
    }

    /**
     * 没有返回值
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        this.executorService.execute(runnable);
    }

    /**
     * 有返回值
     *
     * @param callable
     * @param <T>
     * @return
     */
    public <T> Future<T> submit(Callable<T> callable) {
        return this.executorService.submit(callable);
    }

    /**
     * 线程池关闭 队列里的任务执行完毕之后关闭
     */
    public void shutDown() {
        this.executorService.shutdown();
    }

    /**
     * 立即关闭线程池 线程池状态变为 STOP 队列里的任务会被丢弃
     */
    public void shutDownNow() {
        this.executorService.shutdownNow();
    }

    @PostConstruct
    public void init() {
        logger.info("ThreadPoolUtils----线程池构建成功");
    }

    @PreDestroy
    public void destroyed() {
        this.shutDown();
        logger.info("ThreadPoolUtils----核心线程池销毁成功");
    }

    // --------------------------多线程操作--------------------------

    public <T> void addCompleteFuture(List<CompletableFuture<T>> list, Supplier<T> supplier){
        list.add(CompletableFuture.supplyAsync(supplier,this.executorService));
    }

    public <T> List<T> invokeList(List<CompletableFuture<T>> objects) {
        List<T> objects1 = new ArrayList<>();
        if(CollUtil.isNotEmpty(objects)){
            return objects.stream().map(CompletableFuture::join).collect(Collectors.toList());
        }
        return objects1;
    }
}
