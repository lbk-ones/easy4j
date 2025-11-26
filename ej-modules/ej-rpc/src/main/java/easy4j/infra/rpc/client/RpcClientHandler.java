package easy4j.infra.rpc.client;


import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.FrameType;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.utils.ChannelUtils;
import io.netty.channel.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * rpc客户端 入栈处理器
 *
 * @author bokun
 * @since 2.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ChannelHandler.Sharable
@Slf4j
public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private RpcClient client;

    public RpcClientHandler(RpcClient rpcClient) {
        this.client = rpcClient;
    }

    /**
     * 通道关闭回调
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("current channel inactive");
        this.client.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    /**
     * 客户端接受到消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("current channel receive msg" + msg);
        Transport msg1 = (Transport) msg;
        if (msg1.getFrameType() != FrameType.RESPONSE.getFrameType()) {
            return;
        }
        byte[] body = msg1.getBody();
        if (body == null || body.length == 0) {
            return;
        }
        try {
            ISerializable iSerializable = SerializableFactory.get(client.getClientConfig());
            RpcResponse deserializable = iSerializable.deserializable(body, RpcResponse.class);
            long requestId = deserializable.getRequestId();
            ResFuture future = ResFuture.getFuture(requestId);
            if (null == future) {
                log.error("not found future {}", requestId);
                return;
            }
            future.putResponse(deserializable);
        } catch (Exception ignored) {
        }

    }

    /**
     * 发生异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client appear exception", cause);
        super.exceptionCaught(ctx, cause);
    }
}