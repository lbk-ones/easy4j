package easy4j.infra.rpc.retry;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.exception.RpcException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于LinkedBlockingQueue的消费并重推（指数退避）处理器
 *
 * @param <T> 队列元素类型
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class RetryableQueueConsumer<T> {
    // 核心阻塞队列
    private final LinkedBlockingQueue<RetryableElement<T>> queue;
    // 消费线程池
    private final ExecutorService consumerExecutor;

    // 初始退避时间（毫秒）
    private final long initialBackoffMs;
    // 最大退避时间（避免无限增长）
    private final long maxBackoffMs;
    // 消费线程数
    private final int consumerThreadNum;
    // 优雅停机标识
    private volatile boolean isShutdown = false;

    /**
     * 可重试的队列元素包装类
     * 包含原始数据+已重试次数
     */
    public static class RetryableElement<T> {
        // 原始业务数据
        @Getter
        private final T data;
        // 已重试次数（原子类型保证并发安全）
        private final AtomicInteger retryCount;

        @Getter
        // 最大重试次数（超过则放弃）
        private final int maxRetryCount;

        public RetryableElement(T data, int maxRetryCount) {
            this.data = data;
            this.retryCount = new AtomicInteger(0);
            this.maxRetryCount = maxRetryCount;
        }

        public int getRetryCount() {
            return retryCount.get();
        }

        public void incrementRetryCount() {
            retryCount.incrementAndGet();
        }
    }

    /**
     * 构造函数
     *
     * @param initialBackoffMs  初始退避时间（ms）
     * @param maxBackoffMs      最大退避时间（ms）
     * @param consumerThreadNum 消费线程数
     * @param queueCapacity     队列容量
     * @param threadPrefix      线程前缀
     */
    public RetryableQueueConsumer(long initialBackoffMs, long maxBackoffMs,
                                  int consumerThreadNum, int queueCapacity, String threadPrefix) {
        this.initialBackoffMs = initialBackoffMs;
        this.maxBackoffMs = maxBackoffMs;
        this.consumerThreadNum = consumerThreadNum;
        this.queue = new LinkedBlockingQueue<>(queueCapacity);

        // 初始化消费线程池（核心线程数=消费线程数，避免频繁创建销毁）
        this.consumerExecutor = new ThreadPoolExecutor(
                consumerThreadNum,
                consumerThreadNum,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private final AtomicInteger threadNum = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, StrUtil.blankToDefault(threadPrefix, "") + "queue-consumer-" + threadNum.getAndIncrement());
                        thread.setDaemon(false); // 非守护线程，保证消费完成
                        thread.setPriority(Thread.NORM_PRIORITY);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.DiscardPolicy() // 拒绝策略（可根据业务调整）
        );
    }

    /**
     * 添加元素到队列
     *
     * @param data 原始业务数据
     * @throws InterruptedException 中断异常
     */
    public void add(T data, int retryCount) throws InterruptedException {
        if (isShutdown) {
            throw new IllegalStateException("The queue processor has been shut down and cannot accept elements");
        }
        if (retryCount > 0) {
            queue.put(new RetryableElement<>(data, retryCount));
        }
    }

    /**
     * 启动消费,每个元素都有自己的重试次数和退避指数
     *
     * @param handler 业务处理逻辑（抛出异常则视为处理失败）
     */
    public void startConsume(ConsumerHandler<T> handler) {
        for (int i = 0; i < consumerThreadNum; i++) {
            consumerExecutor.submit(() -> {
                while (!isShutdown && !Thread.currentThread().isInterrupted()) {
                    try {
                        RetryableElement<T> element = queue.take();
                        try {
                            handler.handle(element.getData());
                        } catch (Throwable e) {
                            String errorMsg = determineException(e);
                            if (errorMsg == null) {
                                continue;
                            }
                            int currentRetry = element.getRetryCount();
                            int maxRetryTimes = element.getMaxRetryCount();

                            if (currentRetry >= maxRetryTimes) {
                                log.error("The retry count for element [{}] has reached the maximum limit [{}]. Processing is abandoned. Exception: {}",
                                        element.getData(), maxRetryTimes, errorMsg);
                                continue;
                            }

                            // 计算指数退避时间：initialBackoffMs * (2^currentRetry)
                            long backoffMs = initialBackoffMs * (1L << currentRetry);
                            backoffMs = Math.min(backoffMs, maxBackoffMs);
                            TimeUnit.MILLISECONDS.sleep(backoffMs);
                            element.incrementRetryCount();
                            queue.put(element);
                            log.info("Element [{}] processing failed, retry [{}] times, backoff [{}] ms and retry, exception:{}",
                                    element.getData().getClass().getSimpleName(), currentRetry, backoffMs, errorMsg);
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("The consumption thread has been interrupted and is preparing to exit:" + Thread.currentThread().getName());
                        break;
                    } catch (Exception ex) {
                        log.error("Abnormal consumption thread:" + ex.getMessage());
                    }
                }
            });
        }
    }

    /**
     * 决定是否要重试，如果返回的字符串不为null那么就代表需要重试
     *
     * @param e 要决断的异常
     * @return String
     */
    public static String determineException(Throwable e) {
        boolean flag = false;
        Throwable cause = e.getCause();
        if (e instanceof RpcException rpcException) {
            return rpcException.isWillRetry() ? "throw exception to retry" : null;
        }
        if (
                e instanceof TimeoutException ||
                        e instanceof RejectedExecutionException ||
                        e instanceof SQLException ||
                        e instanceof IOException
        ) {
            flag = true;
        } else {
            if (cause != null) {
                return determineException(cause);
            }
        }
        return flag ? e.getClass().getName() : null;
    }

    /**
     * 优雅关闭队列处理器
     *
     * @param timeout 关闭超时时间
     * @param unit    时间单位
     * @throws InterruptedException 中断异常
     */
    public void shutdown(long timeout, TimeUnit unit) throws InterruptedException {
        isShutdown = true;
        // 关闭线程池，不再接受新任务
        consumerExecutor.shutdown();
        // 等待线程池处理完现有任务
        if (!consumerExecutor.awaitTermination(timeout, unit)) {
            // 超时后强制关闭
            consumerExecutor.shutdownNow();
        }
        queue.clear();
        log.info("The queue processor【{}】 has gracefully shut down", Thread.currentThread().getName());
    }


    // 测试示例
    public static void main(String[] args) throws InterruptedException {
        // 初始化处理器：最大重试3次，初始退避100ms，最大退避1000ms，2个消费线程，队列容量100
        RetryableQueueConsumer<String> consumer = new RetryableQueueConsumer<>(
                1000,
                1000 * 60,
                1,
                100,
                "test-");

        // 添加测试数据
        for (int i = 0; i < 5; i++) {
            consumer.add("测试数据-" + i, 3);
        }

        // 启动消费（模拟业务处理：随机失败）
        consumer.startConsume(data -> {
            // 模拟50%的失败概率
            if (Math.random() > 0.1) {
                throw new RuntimeException("业务处理失败（模拟）");
            }
            System.out.println("成功处理元素：" + data + "，线程：" + Thread.currentThread().getName());
        });

        // 运行10秒后关闭
        TimeUnit.MINUTES.sleep(10);
        consumer.shutdown(5, TimeUnit.SECONDS);
    }
}