package easy4j.infra.rpc.client;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.codec.Codec;
import easy4j.infra.rpc.codec.RpcDecoder;
import easy4j.infra.rpc.codec.RpcEncoder;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.config.NettyBootStrap;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.FrameType;
import easy4j.infra.rpc.enums.LbType;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.exception.RpcTimeoutException;
import easy4j.infra.rpc.heart.NettyHeartbeatHandler;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.server.DefaultServerNode;
import easy4j.infra.rpc.server.Node;
import easy4j.infra.rpc.server.ServerNode;
import easy4j.infra.rpc.utils.ChannelUtils;
import easy4j.infra.rpc.utils.Host;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


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

    @Getter
    private final E4jRpcConfig rpcConfig;
    private final Bootstrap bootstrap = new Bootstrap();
    private final RpcClientHandler rpcClientHandler;

    @Getter
    private static final Map<Host, Channel> cacheChannel = new ConcurrentHashMap<>(128);

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    @Getter
    private EventLoopGroup workerGroup;
    private final NettyHeartbeatHandler nettyHeartbeatHandler;
    private final LoggingHandler loggingHandler;

    private final LengthFieldPrepender lengthFieldPrepender;

    private final ServerNode serverNode;

    /**
     * @param rpcConfig 客户端配置
     */
    public RpcClient(E4jRpcConfig rpcConfig) {
        super(true);
        this.rpcConfig = rpcConfig;
        rpcClientHandler = new RpcClientHandler(this);
        this.nettyHeartbeatHandler = new NettyHeartbeatHandler(false);
        this.loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        this.lengthFieldPrepender = new LengthFieldPrepender(4);
        this.serverNode = DefaultServerNode.INSTANCE;
        start();
    }


    // 初始化客户端连接
    private void start() {
        if (isStarted.compareAndSet(false, true)) {
            workerGroup = getEventLoop(1);
            final RpcClient rpcClient = this;
            this.bootstrap
                    .group(workerGroup)
                    .channel(getMainClientChannel())
                    .option(ChannelOption.SO_KEEPALIVE, rpcConfig.isSoKeepLive())
                    .option(ChannelOption.TCP_NODELAY, rpcConfig.isTcpNodeDelay())
                    //.option(ChannelOption.SO_TIMEOUT, rpcConfig.getSoTimeOut())
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, rpcConfig.getClient().getConnectTimeOutMillis())
                    .option(ChannelOption.SO_SNDBUF, rpcConfig.getSoSndBuf())
                    .option(ChannelOption.SO_RCVBUF, rpcConfig.getSoRcvBuf())
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(rpcConfig.getWriteBufferLowWaterMark(), rpcConfig.getWriteBufferHighWaterMark()))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(loggingHandler);
                            // IdleStateHandler：45秒读空闲（服务端没响应），10秒写空闲（主动发心跳）
                            pipeline.addLast(new IdleStateHandler(45, 10, 0, TimeUnit.SECONDS));
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
                            pipeline.addLast(nettyHeartbeatHandler);
                            pipeline.addLast(rpcClientHandler);
                            pipeline.addLast(new RpcReconnectHandler(rpcClient));
                            pipeline.addLast(lengthFieldPrepender);
                        }
                    });
        }


    }

    /**
     * 获取指定主机通道
     *
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
                log.info("netty client auto closed");
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

    public RpcResponse sendRequestSync(RpcRequest request) {
        String serviceName = request.getServiceName();
        if (StrUtil.isBlank(serviceName))
            return RpcResponse.error(request.getRequestId(), RpcResponseStatus.SERVICE_NAME_NOT_BE_NULL);
        // adapter hot reload
        LbType lbType = IntegratedFactory.getRpcConfig().getConfig().getLbType();
        Node node = serverNode.selectNodeByServerName(serviceName, lbType);
        if (node != null) {
            Host host = node.getHost();
            return sendRequestSync(request, host);
        } else {
            throw new RpcException("the servername " + serviceName + " not have available host !");
        }
    }

    public RpcResponse sendRequestSync(RpcRequest request, Host host) {
        Channel channel = getChannel(host);
        if (channel == null) {
            throw new RpcException("not obtain get channel from:" + host);
        }
        ISerializable iSerializable = SerializableFactory.get();
        ResFuture resFuture = new ResFuture(request.getRequestId(), rpcConfig.getClient().getInvokeTimeOutMillis());
        Transport transport = Transport.of(FrameType.REQUEST, iSerializable.serializable(request));
        channel.writeAndFlush(transport).addListener(e -> {
            if (e.isSuccess()) {
                resFuture.setSendOk(true);
                return;
            } else {
                resFuture.setSendOk(false);
            }
            Throwable cause = e.cause();
            resFuture.setCause(cause);
            resFuture.putResponse(null);
        });
        try {
            RpcResponse rpcResponse = resFuture.waitResponse();
            if (null == rpcResponse) {
                if (resFuture.isSendOk()) {
                    if (resFuture.isTimeout()) {
                        log.error("wait response on the channel {} timeout {}", host.getAddress(), rpcConfig.getClient().getInvokeTimeOutMillis());
                        throw new RpcTimeoutException(host.getAddress(), rpcConfig.getClient().getInvokeTimeOutMillis());
                    }
                } else {
                    throw new RpcException(host.toString(), resFuture.getCause());
                }
            }
            return rpcResponse;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * create channel
     *
     * @param host   host
     * @param isSync sync flag
     * @return channel
     */
    public Channel createChannel(Host host, boolean isSync) {
        try {
            ChannelFuture future;
            synchronized (bootstrap) {
                future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            }
            if (isSync) {
                Channel channel = future.sync().channel();
                cacheChannel.put(host, channel);
                return channel;
            } else {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                future.addListener((ChannelFutureListener) e -> {
                    if (e.isSuccess()) {
                        Channel channel = e.channel();
                        log.info("async obtain get channel: " + ChannelUtils.toAddress(channel));
                        cacheChannel.putIfAbsent(host, channel);
                        countDownLatch.countDown();
                    } else {
                        log.error("async get connection error {}", host);
                    }
                });
                if (!countDownLatch.await(rpcConfig.getClient().getConnectTimeOutMillis(), TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Connect to host: " + host + " timeout");
                }
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Connect to host: " + host + " failed", e);
        }
    }

    public static void main(String[] args) throws Exception {
        Method method = TestHello.class.getDeclaredMethod("testHello", String.class);
        RpcRequest rpcRequest = RpcRequest.of(method, new Object[]{"xxx"}, null);
        Host host = Host.of("127.0.0.1:8111");
        RpcResponse rpcResponse;
        try (RpcClient rpcClient = RpcClientFactory.getClient()) {
            rpcResponse = rpcClient.sendRequestSync(rpcRequest, host);
            System.out.println(rpcResponse);
            TimeUnit.MINUTES.sleep(100);
        }
    }
}