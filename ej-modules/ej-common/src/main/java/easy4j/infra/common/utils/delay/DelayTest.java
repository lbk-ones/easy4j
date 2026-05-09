package easy4j.infra.common.utils.delay;

import java.util.concurrent.TimeUnit;

public class DelayTest {
    public static void main(String[] args) throws InterruptedException {
        // 初始化全局唯一延时执行器

        System.out.println("主线程启动：" + System.currentTimeMillis());

        // 提交延时任务：3秒后执行
        DelayExecutor.instance.submit("订单10086", 3000, (data) -> {
            System.out.println("延时3秒执行订单关闭：" + System.currentTimeMillis());
            System.out.println("业务参数：" + data);
        });

        // 提交另一个任务：5秒后执行
        DelayExecutor.instance.submit("消息通知", 5000, (data) -> {
            System.out.println("延时5秒发送通知：" + System.currentTimeMillis());
        });

        // 主线程等待观察
        TimeUnit.SECONDS.sleep(10);
        // 关闭
        DelayExecutor.instance.shutdown();
    }
}