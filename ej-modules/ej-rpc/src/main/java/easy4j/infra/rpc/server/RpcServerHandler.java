package easy4j.infra.rpc.server;

import easy4j.infra.rpc.config.ServerConfig;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.FrameType;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.exception.DecodeRpcException;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
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
     * 通道接受到消息
     *
     * @param ctx channel 上下文
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Transport msg1 = (Transport) msg;
        log.info("current channel receive msg msgType is " + msg1.getFrameType() + " body -> " + new String(msg1.getBody(), StandardCharsets.UTF_8));
        RpcRequest request = rpcServer.getRequest(ctx);
        long requestId = request.getRequestId();
        try {
            Optional.ofNullable(executorService)
                    .ifPresent(e -> e.execute(() -> {
                        ServerMethodInvoke serverMethodInvoke = new ServerMethodInvoke(request);
                        RpcResponse invoke = serverMethodInvoke.invoke();
                        send(ctx,invoke);
                    }));
        } catch (RejectedExecutionException exception) {
            RpcResponse error = RpcResponse.error(requestId, RpcResponseStatus.RESOURCE_EXHAUSTED);
            send(ctx, error);
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
    private void send(ChannelHandlerContext ctx, RpcResponse result) {
        ISerializable iSerializable = SerializableFactory.get(this.rpcServer.getServerConfig());
        Transport transport = Transport.of(FrameType.RESPONSE, iSerializable.serializable(result));
        ctx.channel().writeAndFlush(transport).addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                Throwable cause = channelFuture.cause();
                log.error(result.getCode() + " res error ", cause);
            }
        });
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
        RpcRequest request = rpcServer.getRequest(ctx);
        if (cause instanceof DecodeRpcException) {
            send(ctx, RpcResponse.error(request.getRequestId(), RpcResponseStatus.DECODE_ERROR, cause.getMessage()));
        } else {
            send(ctx, RpcResponse.error(request.getRequestId(), RpcResponseStatus.SYSTEM_ERROR, cause));
        }
    }
}