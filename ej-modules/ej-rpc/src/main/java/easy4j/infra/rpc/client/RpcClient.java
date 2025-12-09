package easy4j.infra.rpc.client;
import java.util.HashMap;

import cn.hutool.core.date.SystemClock;
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
import easy4j.infra.rpc.utils.SequenceUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
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
                                    14,
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
            return RpcResponse.error(RpcResponse.ERROR_MSG_ID, RpcResponseStatus.SERVICE_NAME_NOT_BE_NULL);
        // adapter hot reload
        LbType lbType = IntegratedFactory.getConfig().getLbType();
        Node node = serverNode.selectNodeByServerName(serviceName, lbType);
        if (node != null) {
            Host host = node.getHost();
            return sendRequestSync(request, host);
        } else {
            throw new RpcException("the servername " + serviceName + " not have available host !");
        }
    }

    /**
     * 客户端同步发送消息
     * @param request 请求体
     * @param host 要发送到哪个主机
     * @return RpcResponse 服务端响应体
     * @throws RpcException
     */
    public RpcResponse sendRequestSync(RpcRequest request, Host host) throws RpcException {
        Channel channel = getChannel(host);
        if (channel == null) {
            throw new RpcException("not obtain get channel from:" + host);
        }
        long beginTime = SystemClock.now();
        ISerializable iSerializable = SerializableFactory.get();
        Transport transport = Transport.of(FrameType.REQUEST, iSerializable.serializable(request));
        long msgId = transport.getMsgId();
        if (log.isDebugEnabled()) {
            log.debug("begin send request info {}",transport);
        }
        ResFuture resFuture = new ResFuture(msgId, rpcConfig.getClient().getInvokeTimeOutMillis());
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
                        throw new RpcTimeoutException(host.getAddress(), rpcConfig.getClient().getInvokeTimeOutMillis())
                                .setMsgId(msgId);
                    }
                } else {
                    throw new RpcException(host.toString(), resFuture.getCause()).setMsgId(msgId);
                }
            }else{
                long cost = SystemClock.now() - beginTime;
                rpcResponse.setCost(cost);
            }
            return rpcResponse;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RpcException(e).setMsgId(msgId);
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
                        Channel channel1 = cacheChannel.get(host);
                        if(channel1==null){
                            cacheChannel.putIfAbsent(host, channel);
                        }else{
                            channel.attr(ChannelUtils.IS_RECONNECT).set(false);
                            channel.close();
                        }
                        countDownLatch.countDown();
                    } else {
                        log.error("async get connection error {}", host);
                    }
                });
                if (!countDownLatch.await(rpcConfig.getClient().getConnectTimeOutMillis(), TimeUnit.MILLISECONDS)) {
                    throw new RpcTimeoutException("Connect to host: " + host + " timeout");
                }
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RpcException("Connect to host: " + host + " failed", e);
        }
    }

    public static void main(String[] args) throws Exception {
        Method method = TestHello.class.getDeclaredMethod("testHello", String.class);
        Host host = Host.of("127.0.0.1:8111");
        RpcClient rpcClient = RpcClientFactory.getClient();
        System.err.println("res----" + rpcClient.sendRequestSync(RpcRequest.of(method, new Object[]{"xxx1"}, null), host));
        System.err.println("res----" + rpcClient.sendRequestSync(RpcRequest.of(method, new Object[]{"xxx2"}, null), host));
        System.err.println("res----" + rpcClient.sendRequestSync(RpcRequest.of(method, new Object[]{"xxx3"}, null), host));
        System.err.println("res void----" + rpcClient.sendRequestSync(RpcRequest.of(TestHello.class.getDeclaredMethod("testVoid", String.class), new Object[]{"xxx4 void"}, null), host));
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(null);
        rpcRequest.setClassIdentify(TestHello.class.getName());
        rpcRequest.setMethodName("testVoid3");
        rpcRequest.setParameterTypes(new String[]{String.class.getName()});
        rpcRequest.setParameters(new Object[]{"test hahahah"});
        rpcRequest.setReturnType(void.class.getName());
        rpcRequest.setAttachment(new HashMap<String,Object>());
        System.err.println("res void2----" + rpcClient.sendRequestSync(rpcRequest, host));
        TimeUnit.MINUTES.sleep(60L);
    }
}