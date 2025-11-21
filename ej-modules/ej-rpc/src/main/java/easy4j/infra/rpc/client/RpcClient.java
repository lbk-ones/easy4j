package easy4j.infra.rpc.client;

import easy4j.infra.rpc.codec.RpcDecoder;
import easy4j.infra.rpc.codec.RpcEncoder;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;


/**
 * rpc客户端
 * @author bokun
 * @since 2.0.1
 */
public class RpcClient {
    private final String host;
    private final int port;
    private Channel channel;

    /**
     *
     * @param host
     * @param port
     */
    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    public void reset(){
        RpcClientHandler rpcClientHandler = channel.pipeline().get(RpcClientHandler.class);
        rpcClientHandler.reset();
    }

    // 初始化客户端连接
    private void init() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, false)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // 1. RPC编码器：将RpcRequest对象序列化为ByteBuf
                        pipeline.addLast(new RpcEncoder());

                        // 2. 长度字段编码器：自动给请求添加“数据长度”字段（配合服务端拆包）
                        pipeline.addLast(new LengthFieldPrepender(4));

                        // 3. 拆包解码器（与服务端配置完全一致！否则解析失败）
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(
                            1024 * 1024 * 10, // 最大包长度10MB
                            4 + 1 + 1, // 长度字段偏移量（6字节）
                            4, // 长度字段字节数（4字节）
                            1, // 长度调整值（+1字节校验和）
                            0, // 不跳过初始字节（RpcDecoder需要解析完整头部）
                            false
                        ));

                        // 4. RPC解码器：将拆包后的ByteBuf解析为RpcResponse对象
                        pipeline.addLast(new RpcDecoder());

                        // 5. 业务处理器：处理服务端响应
                        pipeline.addLast(new RpcClientHandler());
                    }
                });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            this.channel = future.channel();
            System.out.println("RPC客户端连接成功：" + host + ":" + port);
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            throw new RuntimeException("客户端初始化失败", e);
        }
    }

    // 发送RPC请求（同步示例，实际可用Future异步处理）
    public RpcResponse sendRequest(RpcRequest request) throws InterruptedException {
        RpcClientHandler handler = channel.pipeline().get(RpcClientHandler.class);
        handler.setRequest(request);
        channel.writeAndFlush(request).sync();
        // 等待响应（实际用CountDownLatch或CompletableFuture异步等待）
        synchronized (handler) {
            handler.wait();
        }
        return handler.getResponse();
    }

    // 客户端业务处理器：接收RpcResponse
    static class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
        private RpcRequest request;
        private RpcResponse response;

        private CountDownLatch countDownLatch = new CountDownLatch(1);


        public void reset(){
            countDownLatch = new CountDownLatch(1);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
            // 匹配请求ID（防止多请求乱序，实际用Map<requestId, CompletableFuture>）
            if (response.getRequestId().equals(this.request.getRequestId())) {
                this.response = response;
                synchronized (this) {
                    this.notify(); // 唤醒等待的发送线程
                }
            }
        }

        // getter/setter
        public void setRequest(RpcRequest request) { this.request = request; }
        public RpcResponse getResponse() { return response; }
    }

    public static void main(String[] args) throws Exception {
        RpcClient client = new RpcClient("127.0.0.1", 8888);
        // 构造RPC请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setServiceName("UserService");
        request.setMethodName("getUser");
        request.setParameterTypes(new Class[]{String.class});
        request.setParameters(new Object[]{"123"});
        // 发送请求并获取响应
        RpcResponse response = client.sendRequest(request);
        System.out.println("RPC响应：" + response.getResult());
    }
}