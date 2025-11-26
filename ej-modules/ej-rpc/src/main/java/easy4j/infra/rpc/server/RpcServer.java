package easy4j.infra.rpc.server;

import easy4j.infra.rpc.codec.Codec;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Slf4j
public class RpcServer extends NettyBootStrap {

    public final ServerConfig serverConfig;
    public final ServerBootstrap bootstrap;
    public final RpcServerHandler rpcServerHandler;
    public final AtomicBoolean isStart = new AtomicBoolean(false);

    public RpcServer(ServerConfig serverConfig) {
        super(false);
        this.serverConfig = serverConfig;
        this.bootstrap = new ServerBootstrap();
        this.rpcServerHandler = new RpcServerHandler(this);
    }

    public void start(){
        isStart.compareAndExchange(false, true);
        EventLoopGroup bossGroup = getEventLoop(1);
        EventLoopGroup workerGroup = getEventLoop(0);
        try {
            this.bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(getMainServerChannel())
                    .option(ChannelOption.SO_BACKLOG, this.serverConfig.getSoBackLog())
                    .option(ChannelOption.SO_REUSEADDR,  this.serverConfig.isSoReuseAddr())
                    .childOption(ChannelOption.SO_KEEPALIVE, this.serverConfig.isSoKeepLive())
                    .childOption(ChannelOption.TCP_NODELAY, this.serverConfig.isTcpNodeDelay())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                    Codec.MAX_FRAME_LENGTH,
                                    6,
                                    4,
                                    2,
                                    0,
                                    true
                            ));
                            pipeline.addLast(new RpcDecoder());
                            pipeline.addLast(rpcServerHandler);
                            pipeline.addLast(new RpcEncoder());
                            pipeline.addLast(new LengthFieldPrepender(4)); // 数据长度字段占4字节
                        }
                    });

            ChannelFuture future = bootstrap.bind(serverConfig.getPort()).sync();

            System.out.println("RPC服务端启动，端口：" + serverConfig.getPort());
            future.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("netty server start error",e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ServerConfig build = new ServerConfig()
                .setPort(8888);
        new RpcServer(build).start();
    }
}