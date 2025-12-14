package easy4j.infra.rpc.registry;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import easy4j.infra.rpc.client.RpcClient;
import easy4j.infra.rpc.client.RpcClientFactory;
import easy4j.infra.rpc.config.CommonConstant;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.registry.jdbc.ServiceManagement;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.utils.DefaultUncaughtExceptionHandler;
import easy4j.infra.rpc.utils.Host;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 服务治理控制
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class ServiceControl {

    private static final LinkedBlockingQueue<Pair<Host,ServiceManagement>> SEND_ERROR_QUEUE = new LinkedBlockingQueue<>();


    public static final ServiceControl INSTANCE = new ServiceControl();

    private static final Cache<String, ServiceManagement> SERVICE_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES) // 写入后 30 分钟过期
            .maximumSize(10_000) // 最大缓存容量 10000 条（超量后触发淘汰）
            .build();

    final Registry registry;

    private ServiceControl() {
        registry = RegistryFactory.get();

        startReFlush();
    }

    private void startReFlush() {
        ExecutorService executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("service-control-", null, true, DefaultUncaughtExceptionHandler.getInstance()));
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    try {
                        Pair<Host, ServiceManagement> take1 = SEND_ERROR_QUEUE.take();
                        Host key = take1.getKey();
                        ServiceManagement value = take1.getValue();
                        RpcClient client1 = RpcClientFactory.getClient();
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.serverHandler(CommonConstant.SERVER_FLUSH_CACHE);
                        rpcRequest.setParameters(new Object[]{value});
                        client1.sendRequestASync(rpcRequest, key).addListener((ChannelFutureListener) future -> {
                            if (!future.isSuccess()) {
                                log.error("flush cache appear error " + key, future.cause());
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            }
        });
    }

    public String getServiceKey(String name, Host host) {
        return RegisterInfoType.SERVICE.wrap(name, host);
    }

    public String getNodeKey(String name, Host host) {
        return RegisterInfoType.NODE.wrap(name, host);
    }

    /**
     * 获取 【serviceName】这个服务 的这个【host】主机的服务信息
     *
     * @param serviceName 要获取的服务名称
     * @param host        要获取的服务ip信息
     * @return ServiceManagement
     */
    public ServiceManagement get(String serviceName, Host host) {
        if (StrUtil.isBlank(serviceName) || host == null) {
            throw new RpcException("serviceName and host is should not null!");
        }
        String path = getServiceKey(serviceName, host);
        ServiceManagement ifPresent = SERVICE_CACHE.get(path, (e) -> null);
        if (ifPresent == null) {
            try {
                String s = registry.get(path);
                if (StrUtil.isNotBlank(s)) {
                    ISerializable jackson = SerializableFactory.getJackson();
                    ServiceManagement deserializable = jackson.deserializable(s.getBytes(StandardCharsets.UTF_8), ServiceManagement.class);
                    SERVICE_CACHE.put(path, deserializable);
                    ifPresent = deserializable;
                }
            } catch (Exception e) {
                log.error("Service Control Get Function Appear Error", e);
            }
        }
        return ifPresent == null ? getDefault(serviceName, host) : ifPresent;
    }

    public synchronized List<String> getAllService() {
        Collection<String> children = registry.children(RegisterInfoType.NODE.getRegisterPath());
        return new ArrayList<>(children);
    }

    public synchronized List<Host> getAllServiceHost() {
        Collection<String> children = registry.children(RegisterInfoType.NODE.getRegisterPath());
        List<String> allHost = new ArrayList<>();
        for (String child : children) {
            String wrap = RegisterInfoType.NODE.wrap(child, null);
            Collection<String> children1 = registry.children(wrap);
            if (children1 != null && children1.size() > 1) {
                allHost.addAll(children1);
            }
        }
        return allHost.stream().map(Host::new).collect(Collectors.toList());
    }

    public ServiceManagement getDefault(String serviceName, Host host) {
        ServiceManagement deserializable = new ServiceManagement();
        deserializable.setServiceName(serviceName);
        deserializable.setHost(host.getIp());
        deserializable.setPort(host.getPort());
        deserializable.setDisabled(false);
        deserializable.setWeight(1);
        E4jRpcConfig config = IntegratedFactory.getConfig();
        Long invokeTimeOutMillis = config.getClient().getInvokeTimeOutMillis();
        deserializable.setInvokeTimeOutMillis(invokeTimeOutMillis);
        deserializable.setLbType(config.getLbType().name());
        return deserializable;
    }

    /**
     * 服务治理控制
     * 更改，并通知缓存刷新
     * 指定一个主机和服务
     *
     * @param serviceName 服务名称
     * @param host        主机信息(为null更新这个服务所有实例)
     * @param consumer    更改回调
     * @return boolean
     */
    public synchronized boolean control(String serviceName, Host host, Consumer<ServiceManagement> consumer) {
        try {
            String path = getServiceKey(serviceName, null);
            Collection<String> children = registry.children(path);
            for (String child : children) {
                if (StrUtil.isBlank(child)) continue;
                Host host1 = new Host(child);
                if (host != null && !host.equals(host1)) {
                    continue;
                }
                String pathInfo = RegisterInfoType.SERVICE.wrap(serviceName, host1);
                String s = registry.get(pathInfo);
                ServiceManagement deserializable = null;
                ISerializable jackson = SerializableFactory.getJackson();
                if (StrUtil.isNotBlank(s)) {
                    deserializable = jackson.deserializable(s.getBytes(StandardCharsets.UTF_8), ServiceManagement.class);
                } else {
                    deserializable = getDefault(serviceName, host1);
                }
                consumer.accept(deserializable);
                String dStr = new String(jackson.serializable(deserializable), StandardCharsets.UTF_8);
                registry.put(path, dStr, false);
                flushCache(deserializable);
            }

            return true;
        } catch (Exception e) {
            log.error("control error ", e);
            return false;
        }
    }

    public void setCache(String key, ServiceManagement value) {
        SERVICE_CACHE.put(key, value);
    }

    /**
     * 通知所有服务更新对应缓存
     *
     * @param serviceManagement 要更新的服务信息
     */
    public void flushCache(ServiceManagement serviceManagement) {
        if (serviceManagement != null) {
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            long now = SystemClock.now();
            List<Host> allServiceHost = getAllServiceHost();
            for (Host host1 : allServiceHost) {
                try {
                    RpcClient client1 = RpcClientFactory.getClient();
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.serverHandler(CommonConstant.SERVER_FLUSH_CACHE);
                    rpcRequest.setParameters(new Object[]{serviceManagement});
                    client1.sendRequestASync(rpcRequest, host1).addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            log.error("flush cache appear error " + host1, future.cause());
                            SEND_ERROR_QUEUE.add(new Pair<>(host1,serviceManagement.clone()));
                        }
                    });
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("flush cache appear error " + host1, e);
                }
            }
            long now2 = SystemClock.now();
            log.info("flush cache success cost {}ms flush success {} error {}", (now2 - now), successCount.get(), errorCount.get());
        }
    }


}
