package easy4j.infra.rpc.client;


import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.FrameType;
import easy4j.infra.rpc.exception.DecodeRpcException;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import io.netty.channel.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

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
public class RpcClientHandler extends ChannelDuplexHandler {

    private RpcClient client;


    public RpcClientHandler(RpcClient rpcClient) {
        this.client = rpcClient;
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
        Transport msg1 = (Transport) msg;
//        if (log.isInfoEnabled()) {
//            log.info("current client channel {} receive msg: " + msg1, ctx.channel().id());
//        }
        if (msg1.getFrameType() != FrameType.RESPONSE.getFrameType()) {
            return;
        }
        byte[] body = msg1.getBody();
        if (body == null || body.length == 0) {
            return;
        }
        if (log.isInfoEnabled()) {
            log.info("current client channel {} receive msg header {}, receive msg body {}", ctx.channel().id(), msg1, new String(body, StandardCharsets.UTF_8));
        }
        try {
            ISerializable iSerializable = SerializableFactory.get();
            RpcResponse response = iSerializable.deserializable(body, RpcResponse.class);
            long msgId = response.getMsgId();
            ResFuture future = ResFuture.getFuture(msgId);
            if (null == future) {
                log.error("msgId {},not found future", msgId);
                return;
            }
            future.putResponse(response);
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
        if (cause instanceof DecodeRpcException) {
            log.error("decode error , so close channel!");
            ctx.channel().close();
        }
    }
}