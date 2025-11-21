package easy4j.infra.rpc.server;

import easy4j.infra.rpc.codec.RpcDecoder;
import easy4j.infra.rpc.codec.RpcEncoder;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class RpcServer {
    public void start(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, false)
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
                        pipeline.addLast(new RpcServerHandler());

                        // 4. RPC编码器：将RpcResponse对象序列化为ByteBuf
                        pipeline.addLast(new RpcEncoder());

                        // 5. 长度字段编码器：自动给响应添加“数据长度”字段（配合客户端拆包）
                        pipeline.addLast(new LengthFieldPrepender(4)); // 数据长度字段占4字节
                    }
                });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("RPC服务端启动，端口：" + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    // 业务处理器：处理RpcRequest，返回RpcResponse
    static class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
            // 模拟服务调用（实际需通过服务注册中心找到目标服务实例）
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            try {
                // 这里简化：假设调用UserService.getUser("123")
                if ("UserService".equals(request.getServiceName()) && "getUser".equals(request.getMethodName())) {
                    response.setStatus(0);
                    response.setResult("用户信息：ID=" + request.getParameters()[0]);
                } else {
                    response.setStatus(1);
                    response.setErrorMsg("服务或方法不存在");
                }
            } catch (Exception e) {
                response.setStatus(1);
                response.setErrorMsg(e.getMessage());
            }
            // 写回响应（触发RpcEncoder和LengthFieldPrepender）
            ctx.writeAndFlush(response);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new RpcServer().start(8888);
    }
}