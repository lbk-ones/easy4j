package easy4j.infra.rpc.server;

import easy4j.infra.rpc.codec.RpcDecoder;
import easy4j.infra.rpc.codec.RpcEncoder;
import easy4j.infra.rpc.config.NettyBootStrap;
import easy4j.infra.rpc.config.ServerConfig;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
@ChannelHandler.Sharable
public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    public RpcServer rpcServer;
    public RpcServerHandler(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("current channel register");
        super.channelRegistered(ctx);
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("current channel unRegister");
        super.channelUnregistered(ctx);
    }

    /**
     * 通道激活
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
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("current channel inactive");
        super.channelInactive(ctx);
    }

    /**
     * 通道接受到消息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("current channel receive msg"+ msg);
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("current channel complete receive msg");
        super.channelReadComplete(ctx);
    }

    /**
     * 心跳触发
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("current channel userEventTriggered");
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 背压可读状态变更
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