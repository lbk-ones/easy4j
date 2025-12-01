package easy4j.infra.rpc.heart;

import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.FrameType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用心跳处理 Handler（客户端/服务端均可复用，通过构造参数区分角色）
 * 不用处理失败等错误信息 错误会自动向下传递
 * 只发送对面不接受
 *
 * @since 2.0.1
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyHeartbeatHandler extends ChannelInboundHandlerAdapter {
    private final boolean isServer; // true=服务端，false=客户端


    public NettyHeartbeatHandler(boolean isServer) {
        this.isServer = isServer;
    }

    /**
     * 处理空闲事件（IdleStateHandler 触发）
     *
     * @param ctx 上下文
     * @param evt 事件
     * @throws Exception 抛出异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            IdleState state = event.state();
            switch (state) {
                case READER_IDLE:
                    if (isServer) {
                        log.info("The server detected that the client is reading idle and disconnected");
                        ctx.close();
                    } else {
                        log.info("The client detects that the server is reading idle and triggers a reconnection");
                        ctx.close();
                    }
                    break;
                case WRITER_IDLE:
                    if (!isServer) {
                        sendHeartbeat(ctx);
                    }
                    break;
                case ALL_IDLE:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 发送心跳包
     *
     * @param ctx channel 上下文
     */
    private void sendHeartbeat(ChannelHandlerContext ctx) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(Transport.of(FrameType.REQUEST_HEART)).addListener(future -> {
                if (!future.isSuccess()) {
                    Throwable cause = future.cause();
                    log.error("heart send false",cause);
                }
            });
        }
    }
}