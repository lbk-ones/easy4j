package easy4j.infra.rpc.server.handlers;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.FrameType;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.exception.DecodeRpcException;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.server.RpcServer;
import easy4j.infra.rpc.server.RpcServerWrapper;
import easy4j.infra.rpc.server.ServerMethodInvoke;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * 处理服务端响应
 *
 * @author bokun
 * @since 2.0.1
 */
@ChannelHandler.Sharable
@Slf4j
public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    public final RpcServer rpcServer;

    public ExecutorService executorService;

    public RpcServerHandler(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
        this.executorService = rpcServer.getExecutorService();
    }

    /**
     * 通道接受到消息,注意消息的类型
     *
     * @param ctx channel 上下文
     * @param msg 解码出来的消息
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Transport transport = (Transport) msg;
        long msgId = transport.getMsgId();
        byte frameType = transport.getFrameType();
        if (FrameType.REQUEST.getFrameType() != frameType) {
            // heart beat return
            if (FrameType.REQUEST_HEART.getFrameType() == frameType) {
                send(ctx,RpcResponse.success(msgId,RpcResponseStatus.INSTANCE_NOT_FOUND),FrameType.RESPONSE_HEART);
            }else{
                super.channelRead(ctx, msg);
            }
            return;
        }
        try {
            FrameType byFrameType = FrameType.getByFrameType(transport.getFrameType());
            if (byFrameType == null) {
                RpcResponse error = RpcResponse.error(msgId, RpcResponseStatus.RESOURCE_EXHAUSTED);
                send(ctx, error,FrameType.RESPONSE);
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("current channel receive msg[{}] msgType is {} body:{}", msgId, byFrameType.getFrameTypeDesc(), new String(transport.getBody(), StandardCharsets.UTF_8));
            }
            ISerializable iSerializable = SerializableFactory.get();
            RpcRequest request = iSerializable.deserializable(transport.getBody(), RpcRequest.class);
            if (request != null) {
                try {
                    Optional.ofNullable(executorService)
                            .ifPresent(e -> e.execute(() -> {
                                RpcServerWrapper serverMethodInvoke = new RpcServerWrapper(new ServerMethodInvoke(request, transport));
                                try{
                                    RpcResponse invoke = serverMethodInvoke.invoke();
                                    send(ctx, invoke,FrameType.RESPONSE);
                                }catch (Throwable ex2){
                                    send(ctx, RpcResponse.error(msgId,RpcResponseStatus.SERVER_ERROR),FrameType.RESPONSE);
                                }
                            }));
                } catch (RejectedExecutionException exception) {
                    RpcResponse error = RpcResponse.error(msgId, RpcResponseStatus.RESOURCE_EXHAUSTED);
                    send(ctx, error,FrameType.RESPONSE);
                }
            }
        } catch (RpcException e) {
            e.setMsgId(msgId);
            throw e;
        }catch (Throwable e){
            throw new RpcException(e).setMsgId(msgId);
        }
    }

    /**
     * 发送到客户端
     *
     * @param ctx    channel 上下文
     * @param result 要发送的信息
     * @author bokun
     * @since 2.0.1
     */
    private void send(ChannelHandlerContext ctx, RpcResponse result,FrameType frameType) {
        ISerializable iSerializable = SerializableFactory.get();
        Transport transport = Transport.of(frameType, iSerializable.serializable(result));
        if (ctx.channel().isOpen() && ctx.channel().isActive()) {
            ctx.writeAndFlush(transport).addListener((ChannelFutureListener) channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    Throwable cause = channelFuture.cause();
                    log.error(result.getCode() + " res error ", cause);
                }
            });

        }
    }

    /**
     * 发生异常
     *
     * @param ctx   channel 上下文
     * @param cause 异常信息
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("catch exception ", cause);
        if (cause instanceof RpcException ex2) {
            if (ex2.getCause() != null) cause = ex2.getCause();
            send(ctx, RpcResponse.error(ex2.getMsgId(), RpcResponseStatus.DECODE_ERROR, cause.getMessage()),FrameType.RESPONSE);
        }
    }
}