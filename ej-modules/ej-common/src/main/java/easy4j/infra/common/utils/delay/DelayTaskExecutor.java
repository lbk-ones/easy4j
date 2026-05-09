package easy4j.infra.common.utils.delay;

import easy4j.infra.common.utils.ThreadPoolUtils;
import lombok.Getter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 通用延时任务实体
 * @param <T> 业务数据泛型
 */
class DelayTask<T> implements Delayed {
    /** 业务数据 */
    @Getter
    private final T data;
    /** 执行时间戳 毫秒 */
    private final long executeTime;
    /** 任务执行器 */
    private final TaskRunner<T> taskRunner;

    public DelayTask(T data, long delayMs, TaskRunner<T> taskRunner) {
        this.data = data;
        // 当前时间 + 延时时间 = 到期执行时间
        this.executeTime = System.currentTimeMillis() + delayMs;
        this.taskRunner = taskRunner;
    }

    /** 执行任务 */
    public void run() {
        if (taskRunner != null) {
            taskRunner.run(getData());
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remain = executeTime - System.currentTimeMillis();
        return unit.convert(remain, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }
}

/**
 * 通用延时任务执行器
 * 独立后台线程轮询，到期自动执行
 */
public class DelayTaskExecutor {
    // 延时队列
    private final DelayQueue<DelayTask<?>> delayQueue = new DelayQueue<>();
    // 后台消费线程
    private final Thread workerThread;
    // 运行标记
    private volatile boolean running = true;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public DelayTaskExecutor() {
        // 启动后台守护线程
        workerThread = new Thread(this::loopRun, "delay-task-worker");
        workerThread.setDaemon(true);
        workerThread.start();
        threadPoolTaskExecutor = ThreadPoolUtils.createThreadPoolTaskExecutor("delay-task-runner-pool");

    }

    /**
     * 提交延时任务
     * @param data 业务参数
     * @param delayMs 延时多少毫秒后执行
     * @param taskRunner 要执行的业务逻辑
     * @param <T> 业务参数泛型
     */
    public <T> void submit(T data, long delayMs, TaskRunner<T> taskRunner) {
        if (delayMs <= 0) {
            // 无延时直接执行
            taskRunner.run(data);
            return;
        }
        delayQueue.put(new DelayTask<>(data, delayMs, taskRunner));
    }

    /**
     * 循环阻塞获取到期任务并执行
     */
    private void loopRun() {
        while (running) {
            try {
                // 阻塞等待，直到任务到期
                DelayTask<?> task = delayQueue.take();
                // 异步执行任务，不阻塞队列轮询
                threadPoolTaskExecutor.submit(task::run);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // 单个任务异常不影响整体
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止延时任务器
     */
    public void shutdown() {
        running = false;
        workerThread.interrupt();
        threadPoolTaskExecutor.shutdown();
    }
}