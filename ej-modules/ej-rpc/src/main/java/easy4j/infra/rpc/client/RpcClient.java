package easy4j.infra.rpc.client;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.codec.RpcDecoder;
import easy4j.infra.rpc.codec.RpcEncoder;
import easy4j.infra.rpc.config.ClientConfig;
import easy4j.infra.rpc.config.NettyBootStrap;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.utils.Host;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * <p>rpc客户端</p>
 * <pre>
 * 客户端需要满足的功能（所有的方法要保证线程安全）
 * 启动客户端
 * 获取channel
 * 创建channel
 * 关闭channel
 * </pre>
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class RpcClient extends NettyBootStrap implements AutoCloseable {
    private final ClientConfig clientConfig;
    private final Bootstrap bootstrap = new Bootstrap();
    private final RpcClientHandler rpcClientHandler;

    private static final Map<Host, Channel> cacheChannel = new ConcurrentHashMap<>(128);

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private EventLoopGroup workerGroup;

    /**
     * @param clientConfig 客户端配置
     */
    public RpcClient(ClientConfig clientConfig) {
        super(true);
        this.clientConfig = clientConfig;
        String host = this.clientConfig.getHost();
        Integer port = this.clientConfig.getPort();
        if (StrUtil.isEmpty(host) || port == null) {
            throw new RpcException(" host and port is not null ");
        }
        rpcClientHandler = new RpcClientHandler(this);
        start();
    }

    // 初始化客户端连接
    private void start() {

        workerGroup = getEventLoop(1);
        this.bootstrap
                .group(workerGroup)
                .channel(getMainClientChannel())
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepLive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNodeDelay())
                .option(ChannelOption.SO_REUSEADDR, clientConfig.isSoReuseAddr())
                //.option(ChannelOption.SO_BACKLOG, clientConfig.getSoBackLog())
                .option(ChannelOption.SO_TIMEOUT, clientConfig.getSoTimeOut())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeOutMillis())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSoSndBuf())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getSoRcvBuf())
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(clientConfig.getWriteBufferLowWaterMark(), clientConfig.getWriteBufferHighWaterMark()))
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
                        pipeline.addLast(rpcClientHandler);
                    }
                });
        isStarted.compareAndSet(false, true);
    }

    /**
     * 获取指定主机通道
     * @param host 主机信息
     * @return Channel通道
     */
    public Channel getChannel(Host host) {
        Channel channel1 = cacheChannel.get(host);
        if (channel1 != null && channel1.isActive()) {
            return channel1;
        } else {
            return createChannel(host, true);
        }
    }

    /**
     * 关闭指定channel
     *
     * @param host 端口信息
     */
    public void closeChannel(Host host) {
        Channel channel = cacheChannel.get(host);
        if (channel != null) {
            channel.close();
            cacheChannel.remove(host);
        }
    }

    /**
     * 客户端关闭
     */
    @Override
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                closeAllChannels();
                if (workerGroup != null) {
                    this.workerGroup.shutdownGracefully();
                }
                log.info("netty client closed");
            } catch (Exception ex) {
                log.error("netty client close exception", ex);
            }
        }
    }

    public void closeAllChannels() {
        for (Channel channel : cacheChannel.values()) {
            channel.close();
        }
        cacheChannel.clear();
    }

    public RpcResponse sendRequestSync(RpcRequest request){
        String serviceName = request.getServiceName();
        // TODO 从注册中心中拿取连接信息
        return null;
    }

    /**
     * create channel
     *
     * @param host   host
     * @param isSync sync flag
     * @return channel
     */
    private Channel createChannel(Host host, boolean isSync) {
        try {
            ChannelFuture future;
            synchronized (bootstrap) {
                future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            }
            if (isSync) {
                future.sync();
            }
            if (future.isSuccess()) {
                Channel channel = future.channel();
                cacheChannel.put(host, channel);
                return channel;
            }
            throw new IllegalArgumentException("connect to host: " + host + " failed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Connect to host: " + host + " failed", e);
        }
    }

    public static void main(String[] args) throws Exception {
//        RpcClient client = new RpcClient("127.0.0.1", 8888);
//        // 构造RPC请求
//        RpcRequest request = new RpcRequest();
//        request.setRequestId(UUID.randomUUID().toString());
//        request.setServiceName("UserService");
//        request.setMethodName("getUser");
//        request.setParameterTypes(new Class[]{String.class});
//        request.setParameters(new Object[]{"123"});
//        // 发送请求并获取响应
//        RpcResponse response = client.sendRequest(request);
//        System.out.println("RPC响应：" + response.getResult());
    }
}