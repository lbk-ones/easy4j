package easy4j.infra.rpc.client;


import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.utils.ChannelUtils;
import io.netty.channel.*;

import io.netty.handler.timeout.IdleStateEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;


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
     * 通道激活
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("current channel active");
        super.channelActive(ctx);
    }

    /**
     * 通道关闭回调
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("current channel inactive");
        this.client.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    /**
     * 通道接受到消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("current channel receive msg" + msg);
        Transport msg1 = (Transport) msg;
        byte[] body = msg1.getBody();
        ISerializable iSerializable = SerializableFactory.get(client.getClientConfig());
        RpcResponse deserializable = iSerializable.deserializable(body, RpcResponse.class);
        long requestId = deserializable.getRequestId();
        ResFuture future = ResFuture.getFuture(requestId);
        if (null == future) {
            log.error("not found future {}", requestId);
            return;
        }
        future.putResponse(deserializable);
    }

    /**
     * 心跳触发
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {

        }
    }

    /**
     * 背压可读状态变更
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("current channel channelWritabilityChanged");

        super.channelWritabilityChanged(ctx);
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
        cause.printStackTrace();

        super.exceptionCaught(ctx, cause);
    }
}