package easy4j.infra.rpc.server;

import easy4j.infra.rpc.codec.RpcDecoder;
import easy4j.infra.rpc.codec.RpcEncoder;
import easy4j.infra.rpc.config.NettyBootStrap;
import easy4j.infra.rpc.config.ServerConfig;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.exception.DecodeRpcException;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
@Slf4j
public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    public RpcServer rpcServer;

    public RpcServerHandler(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
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
        Transport msg1 = (Transport) msg;
        log.info("current channel receive msg msgType is " + msg1.getFrameType() + " body -> " + new String(msg1.getBody(), StandardCharsets.UTF_8));
        int magic = msg1.getMagic();
        byte version = msg1.getVersion();
        byte frameType = msg1.getFrameType();
        int dataLength = msg1.getDataLength();
        short checkSum = msg1.getCheckSum();
        byte[] body = msg1.getBody();
        ServerConfig serverConfig = rpcServer.getServerConfig();
        ISerializable iSerializable = SerializableFactory.get(serverConfig);
        RpcRequest deserializable = iSerializable.deserializable(body, RpcRequest.class);

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
        if (cause instanceof DecodeRpcException) {
            log.error("decode error , so close channel!", cause);
            ctx.channel().close();
        }
    }
}