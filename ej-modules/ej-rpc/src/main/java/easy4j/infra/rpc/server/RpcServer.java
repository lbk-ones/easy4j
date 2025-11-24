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
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class RpcServer extends NettyBootStrap {

    public final ServerConfig serverConfig;
    public final ServerBootstrap bootstrap;
    public final RpcServerHandler rpcServerHandler;
    public final AtomicInteger isStart = new AtomicInteger(0);

    public RpcServer(ServerConfig serverConfig) {
        super(false);
        this.serverConfig = serverConfig;
        this.bootstrap = new ServerBootstrap();
        this.rpcServerHandler = new RpcServerHandler(this);
    }

    public void start() throws InterruptedException {
        if (isStart.compareAndExchange(0,1) == 0) {
            return;
        }
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
                            // 1. 拆包解码器（核心：按数据长度字段拆分字节流，解决粘包拆包）
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                    1024 * 1024 * 10, // 最大包长度10MB（防止内存溢出）
                                    4 + 1 + 1, // 长度字段的偏移量（魔术字4 + 版本1 + 消息类型1 = 6字节）
                                    4, // 长度字段的字节数（4字节int）
                                    1, // 长度调整值（数据长度字段后还有1字节校验和，需额外读取）
                                    4 + 1 + 1 + 4 + 1, // 跳过的初始字节数（头部总长度11字节，只保留数据部分给RpcDecoder？不，这里填0！因为RpcDecoder需要解析完整头部）
                                    false // 关闭失败快速失败，避免内存泄漏
                            ));

                            // 2. RPC解码器：将拆包后的ByteBuf解析为RpcRequest对象
                            pipeline.addLast(new RpcDecoder());

                            // 3. 业务处理器：执行RPC请求（调用目标服务方法）
                            pipeline.addLast(rpcServerHandler);

                            // 4. RPC编码器：将RpcResponse对象序列化为ByteBuf
                            pipeline.addLast(new RpcEncoder());

                            // 5. 长度字段编码器：自动给响应添加“数据长度”字段（配合客户端拆包）
                            pipeline.addLast(new LengthFieldPrepender(4)); // 数据长度字段占4字节
                        }
                    });

            ChannelFuture future = bootstrap.bind(serverConfig.getPort()).sync();

            System.out.println("RPC服务端启动，端口：" + serverConfig.getPort());
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ServerConfig build = ServerConfig.builder()
                .port(8888)
                .build();
        new RpcServer(build).start();
    }
}