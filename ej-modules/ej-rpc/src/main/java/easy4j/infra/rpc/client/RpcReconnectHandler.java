package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.FrameType;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.utils.ChannelUtils;
import easy4j.infra.rpc.utils.Host;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 客户端重连
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class RpcReconnectHandler extends ChannelInboundHandlerAdapter {

    final RpcClient client;

    // 已重试次数
    private int retryCount = 0;

    private final int maxRetryCount = IntegratedFactory.getConfig().getClient().getReconnectMaxRetryCount();

    public RpcReconnectHandler(RpcClient rpcClient) {
        this.client = rpcClient;
    }

    /**
     * 通道关闭回调
     * 如果服务端关闭或者异常退出都会触发这个回调
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("current channel {} inactive", ctx.channel().id());
        Host address = ChannelUtils.toAddress(ctx.channel());
        this.client.closeChannel(address);
        Boolean isNeedConnect = ChannelUtils.isNeedReconnect(ctx);
        ctx.channel().close();
        if (isNeedConnect) {
            log.info("begin reconnect!!");
            reConnected(address);
        }
    }


    public void reConnected(Host address) {
        Channel channel = RpcClient.getCacheChannel().get(address);
        if (null != channel) return;
        if (maxRetryCount <= 0 || retryCount >= maxRetryCount) {
            log.error("[Reconnect Termination] The maximum number of retries has been reached, stop reconnecting");
            return;
        }
        //  计算指数退避间隔（1s→2s→4s→8s->16...）
        long delay = Math.min(2 * (1L << retryCount), 65);
        retryCount++;

        this.client.getWorkerGroup().schedule(() -> {
            try {
                client.createChannel(address, false);
            } catch (Exception e) {
                reConnected(address);
            }
        }, delay, TimeUnit.SECONDS);
    }
}
