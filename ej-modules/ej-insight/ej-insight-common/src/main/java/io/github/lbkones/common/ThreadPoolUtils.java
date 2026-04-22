package io.github.lbkones.common;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 线程池工具类，提供创建、管理和监控线程池的完整功能
 * 封装了Java并发包中的线程池相关功能，提供更简洁易用的API
 */
public final class ThreadPoolUtils {

    private static final Logger logger = Logger.getLogger(ThreadPoolUtils.class.getName());
    private static final int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_MAX_POOL_SIZE = DEFAULT_CORE_POOL_SIZE * 2;
    private static final long DEFAULT_KEEP_ALIVE_TIME = 60L;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    private static final int DEFAULT_QUEUE_CAPACITY = 1024;
    private static final RejectedExecutionHandler DEFAULT_REJECTION_POLICY = new ThreadPoolExecutor.CallerRunsPolicy();

    // 私有构造方法，防止实例化
    private ThreadPoolUtils() {
        throw new AssertionError("工具类不能实例化");
    }

    /**
     * =====================
     * 线程池创建方法
     * =====================
     */

    /**
     * 创建一个固定大小的线程池
     * @param corePoolSize 核心线程数
     * @return 新创建的线程池
     */
    public static ThreadPoolExecutor fixedThreadPool(int corePoolSize) {
        return new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory(),
                DEFAULT_REJECTION_POLICY
        );
    }

    /**
     * 创建一个缓存线程池
     * @param maxPoolSize 最大线程数
     * @return 新创建的线程池
     */
    public static ThreadPoolExecutor cachedThreadPool(int maxPoolSize) {
        return new ThreadPoolExecutor(
                0,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new DefaultThreadFactory(),
                DEFAULT_REJECTION_POLICY
        );
    }

    /**
     * 创建一个单线程线程池
     * @return 新创建的线程池
     */
    public static ThreadPoolExecutor singleThreadExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory(),
                DEFAULT_REJECTION_POLICY
        );
    }

    /**
     * 创建一个自定义配置的线程池
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param keepAliveTime 线程空闲存活时间
     * @param timeUnit 时间单位
     * @param queueCapacity 队列容量
     * @param rejectionPolicy 拒绝策略
     * @return 新创建的线程池
     */
    public static ThreadPoolExecutor customThreadPool(
            int corePoolSize,
            int maxPoolSize,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueCapacity,
            RejectedExecutionHandler rejectionPolicy) {
        
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                timeUnit,
                new LinkedBlockingQueue<>(queueCapacity),
                new DefaultThreadFactory(),
                rejectionPolicy
        );
    }

    /**
     * =====================
     * 线程池管理方法
     * =====================
     */

    /**
     * 提交一个任务到线程池
     * @param executor 线程池
     * @param task 任务
     * @param <T> 任务返回类型
     * @return Future对象，用于获取任务执行结果
     */
    public static <T> Future<T> submit(ThreadPoolExecutor executor, Callable<T> task) {
        if (executor == null || task == null) {
            throw new IllegalArgumentException("线程池或任务不能为空");
        }
        return executor.submit(task);
    }

    /**
     * 提交一个无返回值的任务到线程池
     * @param executor 线程池
     * @param task 任务
     */
    public static void execute(ThreadPoolExecutor executor, Runnable task) {
        if (executor == null || task == null) {
            throw new IllegalArgumentException("线程池或任务不能为空");
        }
        executor.execute(task);
    }

    /**
     * 优雅关闭线程池
     * @param executor 线程池
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     */
    public static void shutdownGracefully(ThreadPoolExecutor executor, long timeout, TimeUnit timeUnit) {
        if (executor == null) {
            return;
        }
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, timeUnit)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(timeout, timeUnit)) {
                    logger.warning("线程池未能完全关闭");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * =====================
     * 线程池监控方法
     * =====================
     */

    /**
     * 获取线程池的当前状态
     * @param executor 线程池
     * @return 线程池状态信息
     */
    public static ThreadPoolStatus getStatus(ThreadPoolExecutor executor) {
        if (executor == null) {
            return null;
        }
        
        return new ThreadPoolStatus(
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getTaskCount(),
                executor.getCompletedTaskCount()
        );
    }

    /**
     * 记录线程池的监控指标
     * @param executor 线程池
     */
    public static void logMetrics(ThreadPoolExecutor executor) {
        if (executor == null) {
            return;
        }
        
        ThreadPoolStatus status = getStatus(executor);
        logger.log(Level.INFO, "线程池指标: " + status);
    }

    /**
     * =====================
     * 线程池动态调整
     * =====================
     */

    /**
     * 动态调整线程池的核心线程数
     * @param executor 线程池
     * @param corePoolSize 新的核心线程数
     */
    public static void setCorePoolSize(ThreadPoolExecutor executor, int corePoolSize) {
        if (executor == null) {
            return;
        }
        executor.setCorePoolSize(corePoolSize);
    }

    /**
     * 动态调整线程池的最大线程数
     * @param executor 线程池
     * @param maxPoolSize 新的最大线程数
     */
    public static void setMaxPoolSize(ThreadPoolExecutor executor, int maxPoolSize) {
        if (executor == null) {
            return;
        }
        executor.setMaximumPoolSize(maxPoolSize);
    }

    /**
     * 动态调整线程池的拒绝策略
     * @param executor 线程池
     * @param rejectionPolicy 新的拒绝策略
     */
    public static void setRejectionPolicy(ThreadPoolExecutor executor, RejectedExecutionHandler rejectionPolicy) {
        if (executor == null) {
            return;
        }
        executor.setRejectedExecutionHandler(rejectionPolicy);
    }

    /**
     * =====================
     * 辅助类和内部实现
     * =====================
     */

    /**
     * 线程池状态信息类
     */
    public static class ThreadPoolStatus {
        private final int corePoolSize;
        private final int maxPoolSize;
        private final int poolSize;
        private final int activeCount;
        private final int queueSize;
        private final long taskCount;
        private final long completedTaskCount;

        public ThreadPoolStatus(
                int corePoolSize,
                int maxPoolSize,
                int poolSize,
                int activeCount,
                int queueSize,
                long taskCount,
                long completedTaskCount) {
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.poolSize = poolSize;
            this.activeCount = activeCount;
            this.queueSize = queueSize;
            this.taskCount = taskCount;
            this.completedTaskCount = completedTaskCount;
        }

        @Override
        public String toString() {
            return "ThreadPoolStatus{" +
                    "核心线程数=" + corePoolSize +
                    ", 最大线程数=" + maxPoolSize +
                    ", 当前线程数=" + poolSize +
                    ", 活跃线程数=" + activeCount +
                    ", 队列任务数=" + queueSize +
                    ", 总任务数=" + taskCount +
                    ", 已完成任务数=" + completedTaskCount +
                    '}';
        }
    }

    /**
     * 默认线程工厂，创建具有统一命名的线程
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public DefaultThreadFactory() {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = "e4j-insight-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /**
     * =====================
     * 高级功能
     * =====================
     */

    /**
     * 创建一个具有监控功能的线程池
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param keepAliveTime 线程空闲存活时间
     * @param timeUnit 时间单位
     * @param queueCapacity 队列容量
     * @return 带有监控功能的线程池
     */
    public static ThreadPoolExecutor monitoredThreadPool(
            int corePoolSize,
            int maxPoolSize,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueCapacity) {
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                timeUnit,
                new LinkedBlockingQueue<>(queueCapacity),
                new DefaultThreadFactory(),
                DEFAULT_REJECTION_POLICY
        ) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                logger.log(Level.FINE, "任务开始执行: " + r);
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    logger.log(Level.SEVERE, "任务执行异常: " + r, t);
                } else {
                    logger.log(Level.FINE, "任务执行完成: " + r);
                }
            }
        };
        
        // 定时记录指标
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> logMetrics(executor), 0, 1, TimeUnit.MINUTES);
        
        return executor;
    }

    /**
     * 创建一个具有重试机制的任务
     * @param task 原始任务
     * @param maxRetries 最大重试次数
     * @param retryDelay 重试延迟
     * @param timeUnit 时间单位
     * @param <T> 任务返回类型
     * @return 包装后的任务
     */
    public static <T> Callable<T> withRetry(Callable<T> task, int maxRetries, long retryDelay, TimeUnit timeUnit) {
        return () -> {
            int retries = 0;
            while (true) {
                try {
                    return task.call();
                } catch (Exception e) {
                    if (retries++ >= maxRetries) {
                        throw e;
                    }
                    logger.log(Level.WARNING, "任务执行失败，进行第 " + retries + " 次重试", e);
                    try {
                        timeUnit.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        };
    }

    /**
     * 创建一个具有超时控制的任务
     * @param task 原始任务
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @param <T> 任务返回类型
     * @return 包装后的任务
     */
    public static <T> Callable<T> withTimeout(Callable<T> task, long timeout, TimeUnit timeUnit) {
        return () -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<T> future = executor.submit(task);
            try {
                return future.get(timeout, timeUnit);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new RuntimeException("任务执行超时", e);
            } finally {
                executor.shutdown();
            }
        };
    }
}