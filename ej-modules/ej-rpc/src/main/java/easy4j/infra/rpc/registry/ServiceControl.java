package easy4j.infra.rpc.registry;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import easy4j.infra.rpc.client.RpcClient;
import easy4j.infra.rpc.client.RpcClientFactory;
import easy4j.infra.rpc.config.CommonConstant;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.registry.jdbc.ServiceManagement;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.server.Node;
import easy4j.infra.rpc.utils.Host;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 服务治理控制
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class ServiceControl {

    public static final ServiceControl INSTANCE = new ServiceControl();

    private static final Cache<String, ServiceManagement> SERVICE_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES) // 写入后 5 分钟过期
            .maximumSize(10_000) // 最大缓存容量 10000 条（超量后触发淘汰）
            .build();

    final Registry registry;

    private ServiceControl() {
        registry = RegistryFactory.get();
    }

    public String getKey(String name) {
        return RegisterInfoType.SERVICE.getRegisterPath() + StrPool.SLASH + name;
    }

    public String getNodeKey(String name) {
        return RegisterInfoType.NODE.getRegisterPath() + StrPool.SLASH + name;
    }

    public ServiceManagement get(String serviceName) {
        String path = getKey(serviceName);
        ServiceManagement ifPresent = SERVICE_CACHE.get(path, (e) -> null);
        if (ifPresent == null) {
            String s = registry.get(path);
            if (StrUtil.isNotBlank(s)) {
                ISerializable jackson = SerializableFactory.getJackson();
                ServiceManagement deserializable = jackson.deserializable(s.getBytes(StandardCharsets.UTF_8), ServiceManagement.class);
                SERVICE_CACHE.put(path, deserializable);
            }
        }
        return ifPresent;
    }

    public synchronized List<String> getAllService() {
        Collection<String> children = registry.children(RegisterInfoType.SERVICE.getRegisterPath());
        return new ArrayList<>(children);
    }

    /**
     * 服务治理控制
     * 更改，并通知缓存刷新
     *
     * @param serviceName 服务名称
     * @param consumer    更改回调
     * @return boolean
     */
    public synchronized boolean control(String serviceName, Consumer<ServiceManagement> consumer) {
        try {
            String path = getKey(serviceName);
            String s = registry.get(path);
            ServiceManagement deserializable = null;
            ISerializable jackson = SerializableFactory.getJackson();
            if (StrUtil.isNotBlank(s)) {
                deserializable = jackson.deserializable(s.getBytes(StandardCharsets.UTF_8), ServiceManagement.class);
            } else {
                deserializable = new ServiceManagement();
            }
            consumer.accept(deserializable);
            registry.put(path, new String(jackson.serializable(deserializable), StandardCharsets.UTF_8), false);
            flushCache(serviceName);
            return true;
        } catch (Exception e) {
            log.error("control error ", e);
            return false;
        }
    }

    /**
     * 更新所有服务服务治理控制规则
     *
     * @param consumer 更改回调
     * @return boolean
     */
    public synchronized boolean controlAll(Consumer<ServiceManagement> consumer) {
        try {
            List<String> allService = getAllService();
            for (String s : allService) {
                boolean control = control(s, consumer);
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


    public void flushCache(String serverName) {
        String key = getKey(serverName);
        String s = registry.get(key);
        if (StrUtil.isNotBlank(s)) {
            ServiceManagement deserializable = SerializableFactory.getJackson().deserializable(s.getBytes(StandardCharsets.UTF_8), ServiceManagement.class);
            if (deserializable != null) {
                String nodeKey = getNodeKey(serverName);
                Collection<String> children = registry.children(nodeKey);
                RpcClient client = RpcClientFactory.getClient();
                for (String child : children) {
                    try {
                        Host host = new Host(child);
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.serverHandler(CommonConstant.SERVER_FLUSH_CACHE);
                        rpcRequest.setParameters(new Object[]{deserializable});
                        client.sendRequestASync(rpcRequest, host).addListener((ChannelFutureListener) future -> {
                            if (!future.isSuccess()) {
                                log.error("flush cache send error " + serverName, future.cause());
                            }
                        });
                    } catch (Exception ignored) {
                    }
                }
            }

        }
    }


}
