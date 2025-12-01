package easy4j.infra.rpc.server;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.NamedThreadFactory;
import easy4j.infra.rpc.codec.Codec;
import easy4j.infra.rpc.codec.RpcDecoder;
import easy4j.infra.rpc.codec.RpcEncoder;
import easy4j.infra.rpc.config.NettyBootStrap;
import easy4j.infra.rpc.config.ServerConfig;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.heart.NettyHeartbeatHandler;
import easy4j.infra.rpc.heart.NodeHeartbeatManager;
import easy4j.infra.rpc.server.handlers.ChannelCountHandler;
import easy4j.infra.rpc.server.handlers.RequestHandler;
import easy4j.infra.rpc.server.handlers.RpcServerHandler;
import easy4j.infra.rpc.utils.ChannelUtils;
import easy4j.infra.rpc.utils.DefaultUncaughtExceptionHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * TCP服务
 *
 * @author bokun
 * @since 2.0.1
 */
@Getter
@Slf4j
public class RpcServer extends NettyBootStrap {

    @Getter
    private static long startTime;
    public final ServerConfig serverConfig;
    public final ServerBootstrap bootstrap;
    public final RpcServerHandler rpcServerHandler;
    public final RequestHandler requestHandler;
    public final NettyHeartbeatHandler nettyHeartbeatHandler;
    public final LengthFieldPrepender lengthFieldPrepender;
    public final LoggingHandler loggingHandler;
    public final ChannelCountHandler channelCountHandler;
    public final AtomicBoolean isStart = new AtomicBoolean(false);

    public EventLoopGroup bossGroup;
    public EventLoopGroup workerGroup;
    public RpcServer(ServerConfig serverConfig) {
        super(false);
        this.serverConfig = serverConfig;
        this.bootstrap = new ServerBootstrap();
        this.rpcServerHandler = new RpcServerHandler(this);
        this.nettyHeartbeatHandler = new NettyHeartbeatHandler(true);
        this.lengthFieldPrepender = new LengthFieldPrepender(4);
        this.loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        this.requestHandler = new RequestHandler(this);
        this.channelCountHandler = new ChannelCountHandler();
    }

    public void start() {
        Integer port = serverConfig.getPort();
        ServerPortChannelManager.initPortChannelSet(port);
        isStart.compareAndExchange(false, true);
        EventLoopGroup bossGroup = getEventLoop(1);
        EventLoopGroup workerGroup = getEventLoop(0);
        try {
            this.bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(getMainServerChannel())
                    .option(ChannelOption.SO_BACKLOG, this.serverConfig.getSoBackLog())
                    .option(ChannelOption.SO_REUSEADDR, this.serverConfig.isSoReuseAddr())
                    .childOption(ChannelOption.SO_KEEPALIVE, this.serverConfig.isSoKeepLive())
                    .childOption(ChannelOption.TCP_NODELAY, this.serverConfig.isTcpNodeDelay())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(loggingHandler);
                            // 30s 客户端未发送消息 主动断开客户端连接
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                    Codec.MAX_FRAME_LENGTH,
                                    6,
                                    4,
                                    2,
                                    0,
                                    true
                            ));
                            pipeline.addLast(new RpcDecoder());
                            pipeline.addLast(new RpcEncoder());
                            pipeline.addLast(channelCountHandler);
                            pipeline.addLast(requestHandler);
                            pipeline.addLast(nettyHeartbeatHandler);
                            pipeline.addLast(rpcServerHandler);

                            pipeline.addLast(lengthFieldPrepender); // 数据长度字段占4字节
                        }
                    });
            NodeHeartbeatManager.initPort(port);
            ChannelFuture future = bootstrap.bind(port).sync();
            startTime = System.currentTimeMillis();
            System.out.println("RPC服务端启动，端口：" + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("netty server start error", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 从上下文中获取请求信息
     *
     * @param ctx channel 上下文
     * @return RpcRequest
     */
    public RpcRequest getRequest(ChannelHandlerContext ctx) {
        Attribute<RpcRequest> attr = ctx.channel().attr(ChannelUtils.REQUEST_INFO);
        return attr.get();
    }

    /**
     * @return
     */
    public ExecutorService getExecutorService() {
        int threadNum = Runtime.getRuntime().availableProcessors() * 2 + 1;
        return ExecutorBuilder.create()
                .setCorePoolSize(threadNum)
                .setMaxPoolSize(threadNum * 2)
                .setThreadFactory(new NamedThreadFactory("e4j-server-pool-", null, true, DefaultUncaughtExceptionHandler.getInstance()))
                .build();
    }


    public static void main(String[] args) throws InterruptedException {
        ServerConfig build = new ServerConfig()
                .setPort(8888);
        new RpcServer(build).start();
    }


}