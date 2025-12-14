package easy4j.infra.rpc.server.handlers;

import easy4j.infra.rpc.server.ServerPortChannelManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 自定义 Handler，监听 Channel 生命周期事件
 */
@ChannelHandler.Sharable
public class ChannelCountHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ServerPortChannelManager.addChannel(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ServerPortChannelManager.removeChannel(ctx.channel());
        super.channelInactive(ctx);
    }
}