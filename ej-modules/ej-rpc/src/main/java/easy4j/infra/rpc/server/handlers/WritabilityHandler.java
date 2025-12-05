package easy4j.infra.rpc.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 处理 Channel 可写性变化的 Handler，适配写拥塞场景
 * 核心：写拥塞时暂停生产，数据缓冲到队列；恢复可写后消费队列，保证数据不丢失
 */
@ChannelHandler.Sharable
public class WritabilityHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(WritabilityHandler.class);
    // 业务线程池：用于处理数据生产/发送，避免阻塞 Netty IO 线程
    private static final EventExecutorGroup BUSINESS_EXECUTOR = new DefaultEventExecutorGroup(8);
    // 待发送数据的缓冲队列（限制容量，避免 OOM）
    private static final int QUEUE_CAPACITY = 10000;
    // 暂停状态标记（原子类保证线程安全）
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    // 每个 Channel 绑定独立的发送队列（通过 Attribute 存储，避免多 Channel 共享）
    private final AttributeKey<LinkedBlockingQueue<ByteBuf>> SEND_QUEUE = AttributeKey.valueOf("SEND_QUEUE");
    // 监控指标：累计拥塞次数
    private static final AttributeKey<Integer> CONGESTION_COUNT = AttributeKey.valueOf("CONGESTION_COUNT");

    // ==================== 核心：处理可写性变化 ====================
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        // 仅处理活跃的 Channel，已关闭的直接忽略
        if (!channel.isActive()) {
            log.warn("Channel {} 已非活跃，忽略可写性变化", channel.id().asShortText());
            return;
        }

        boolean writable = channel.isWritable();
        log.debug("Channel {} 可写性变化：{}", channel.id().asShortText(), writable);

        if (!writable) {
            // 1. 写拥塞：仅当未暂停时执行暂停逻辑（避免重复暂停）
            if (isPaused.compareAndSet(false, true)) {
                Integer i = channel.attr(CONGESTION_COUNT).get();
                int congestionCount = i == null ? 0 : i + 1;
                channel.attr(CONGESTION_COUNT).set(congestionCount);
                // 记录监控：拥塞次数、队列当前大小
                log.warn("Channel {} 触发写拥塞，累计拥塞次数：{}，当前缓冲队列大小：{}",
                        channel.id().asShortText(),
                        congestionCount,
                        getSendQueue(channel).size());
                // 异步执行暂停逻辑（避免阻塞 IO 线程）
                BUSINESS_EXECUTOR.execute(() -> pauseDataProduction(channel));
            }
        } else {
            // 2. 恢复可写：仅当已暂停时执行恢复逻辑（避免重复恢复）
            if (isPaused.compareAndSet(true, false)) {
                log.info("Channel {} 恢复可写，开始消费缓冲队列（当前队列大小：{}）",
                        channel.id().asShortText(),
                        getSendQueue(channel).size());
                // 异步执行恢复逻辑（避免阻塞 IO 线程）
                BUSINESS_EXECUTOR.execute(() -> resumeDataProduction(channel));
            }
        }

        // 传递事件给后续 Handler（必须调用，否则后续 Handler 收不到事件）
        super.channelWritabilityChanged(ctx);
    }

    // ==================== 暂停生产：停止主动发送，数据缓冲到队列 ====================
    private void pauseDataProduction(Channel channel) {
        try {
            // 示例逻辑1：停止外部数据源的读取（如 MQ 消费、数据库查询等）
            stopExternalDataSource(channel);
            // 示例逻辑2：标记当前 Channel 为拥塞状态，业务层停止提交新数据
            channel.attr(AttributeKey.valueOf("CONGESTION")).set(true);
            log.info("Channel {} 已暂停数据生产，仅缓冲数据到队列", channel.id().asShortText());
        } catch (Exception e) {
            log.error("Channel {} 暂停数据生产失败", channel.id().asShortText(), e);
        }
    }

    // ==================== 恢复生产：消费缓冲队列，重启数据发送 ====================
    private void resumeDataProduction(Channel channel) {
        if (!channel.isActive()) {
            log.warn("Channel {} 已关闭，清空缓冲队列", channel.id().asShortText());
            getSendQueue(channel).clear();
            return;
        }

        try {
            // 步骤1：重启外部数据源读取
            resumeExternalDataSource(channel);
            // 步骤2：标记 Channel 恢复正常
            channel.attr(AttributeKey.valueOf("CONGESTION")).set(false);

            // 步骤3：消费缓冲队列中的数据（批量发送，避免频繁写操作）
            LinkedBlockingQueue<ByteBuf> queue = getSendQueue(channel);
            ByteBuf batchBuf = Unpooled.buffer();
            int batchSize = 0;
            final int MAX_BATCH_SIZE = 100; // 单次批量发送最大条数

            while (!queue.isEmpty() && batchSize < MAX_BATCH_SIZE) {
                ByteBuf data = queue.poll();
                if (data == null) break;

                // 批量拼接数据（根据业务协议调整，如分隔符、长度字段）
                batchBuf.writeBytes(data);
                data.release(); // 释放缓冲的 ByteBuf，避免内存泄漏
                batchSize++;
            }

            // 发送批量数据
            if (batchBuf.isReadable()) {
                sendData(channel, batchBuf);
                log.info("Channel {} 批量发送缓冲数据：{} 条", channel.id().asShortText(), batchSize);
            } else {
                batchBuf.release();
            }

            // 步骤4：若队列还有数据，继续异步消费（避免单次处理过多）
            if (!queue.isEmpty()) {
                BUSINESS_EXECUTOR.execute(() -> resumeDataProduction(channel));
            }
        } catch (Exception e) {
            log.error("Channel {} 恢复数据生产失败", channel.id().asShortText(), e);
        }
    }

    // ==================== 辅助方法：获取 Channel 绑定的发送队列（懒加载） ====================
    private LinkedBlockingQueue<ByteBuf> getSendQueue(Channel channel) {
        Attribute<LinkedBlockingQueue<ByteBuf>> attr = channel.attr(SEND_QUEUE);
        if (attr.get() == null) {
            attr.set(new LinkedBlockingQueue<>(QUEUE_CAPACITY));
        }
        return attr.get();
    }

    // ==================== 辅助方法：发送数据（带失败回调） ====================
    private void sendData(Channel channel, ByteBuf data) {
        channel.writeAndFlush(data)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.debug("Channel {} 数据发送成功", channel.id().asShortText());
                    } else {
                        log.error("Channel {} 数据发送失败", channel.id().asShortText(), future.cause());
                        // 发送失败：将数据重新放入队列（最多重试3次）
                        ByteBuf retryData = Unpooled.copiedBuffer(data);
                        Attribute<Integer> retryCount1 = channel.attr(AttributeKey.valueOf("RETRY_COUNT"));
                        Integer o = retryCount1.get();
                        int retryCount = o == null ? 0 : o;
                        if (retryCount < 3) {
                            getSendQueue(channel).offer(retryData);
                            retryCount1.set(retryCount + 1);
                        } else {
                            retryData.release();
                            log.error("Channel {} 数据重试次数达上限，丢弃数据", channel.id().asShortText());
                        }
                    }
                });
    }

    // ==================== 模拟：停止外部数据源（如 MQ 消费、HTTP 推送等） ====================
    private void stopExternalDataSource(Channel channel) {
        // 实际业务中：暂停 MQ 消费者、关闭定时任务、停止从上游拉取数据等
        String dataSourceKey = "DATA_SOURCE_" + channel.id().asShortText();
        // 示例：关闭对应的数据源连接/消费者
        // MQConsumer consumer = consumerMap.get(dataSourceKey);
        // if (consumer != null) consumer.pause();
        log.debug("Channel {} 已停止外部数据源", channel.id().asShortText());
    }

    // ==================== 模拟：恢复外部数据源 ====================
    private void resumeExternalDataSource(Channel channel) {
        // 实际业务中：恢复 MQ 消费者、重启定时任务、恢复上游数据拉取等
        String dataSourceKey = "DATA_SOURCE_" + channel.id().asShortText();
        // 示例：恢复对应的数据源连接/消费者
        // MQConsumer consumer = consumerMap.get(dataSourceKey);
        // if (consumer != null) consumer.resume();
        log.debug("Channel {} 已恢复外部数据源", channel.id().asShortText());
    }

    // ==================== 兜底：Channel 关闭时清理资源 ====================
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("Channel {} 已关闭，清理缓冲队列和暂停状态", channel.id().asShortText());
        // 1. 标记为非暂停状态
        isPaused.set(false);
        // 2. 清空缓冲队列，释放 ByteBuf 避免内存泄漏
        LinkedBlockingQueue<ByteBuf> queue = channel.attr(SEND_QUEUE).get();
        if (queue != null) {
            queue.forEach(ByteBuf::release);
            queue.clear();
        }
        // 3. 停止外部数据源，释放资源
        stopExternalDataSource(channel);
        super.channelInactive(ctx);
    }

    // ==================== 异常处理：避免异常扩散导致 Handler 失效 ====================
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Channel {} 发生异常，触发可写性处理兜底", ctx.channel().id().asShortText(), cause);
        // 异常时标记为暂停，避免继续写入数据
        isPaused.set(true);
        // 关闭 Channel，避免资源泄漏
        ctx.close();
    }

    // ==================== 对外提供：提交待发送数据（业务层调用） ====================
    public void submitData(Channel channel, ByteBuf data) {
        if (!channel.isActive()) {
            log.warn("Channel {} 已关闭，丢弃数据", channel.id().asShortText());
            data.release();
            return;
        }

        // 若当前拥塞，缓冲到队列；否则直接发送
        if (isPaused.get() || !channel.isWritable()) {
            try {
                // 队列满时阻塞/丢弃（根据业务选择，此处选择阻塞1秒后丢弃）
                boolean offerSuccess = getSendQueue(channel).offer(data, 1, java.util.concurrent.TimeUnit.SECONDS);
                if (!offerSuccess) {
                    data.release();
                    log.error("Channel {} 缓冲队列已满，丢弃数据", channel.id().asShortText());
                    // 触发告警：队列积压超限
                }
            } catch (InterruptedException e) {
                data.release();
                log.error("Channel {} 提交数据到队列被中断", channel.id().asShortText(), e);
                Thread.currentThread().interrupt();
            }
        } else {
            sendData(channel, data);
        }
    }
}