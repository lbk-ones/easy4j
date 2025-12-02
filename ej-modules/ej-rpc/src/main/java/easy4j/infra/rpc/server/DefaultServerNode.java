package easy4j.infra.rpc.server;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import easy4j.infra.rpc.enums.LbType;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.heart.NodeHeartbeatInfo;
import easy4j.infra.rpc.registry.Event;
import easy4j.infra.rpc.registry.Registry;
import easy4j.infra.rpc.registry.SubscribeListener;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.utils.Host;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 默认节点实现
 */
@Slf4j
public class DefaultServerNode implements ServerNode {

    private static final Cache<String, List<Node>> HOST_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES) // 写入后 5 分钟过期
            .maximumSize(10_000) // 最大缓存容量 10000 条（超量后触发淘汰）
            .build();

    private final Registry registry;


    public DefaultServerNode(Registry registry_) {
        this.registry = registry_;
    }

    @Override
    public List<Node> getNodesByServerName(String serverName) {
        String s = RegisterInfoType.NODE.getRegisterPath() + StrPool.SLASH + serverName;
        return HOST_CACHE.get(serverName,sn->{
            Collection<String> children = registry.children(s);
            return new HashSet<>(children).stream().map(e -> {
                try {
                    String[] split = e.split(StrPool.COLON);
                    String ip = split[0];
                    String port = split[1];
                    String address = ip + StrPool.SLASH + port;
                    String heartInfo = registry.get(address);
                    Node node = new Node(new Host(ip, Integer.parseInt(port)), true);
                    if (StrUtil.isNotEmpty(heartInfo)) {
                        ISerializable jackson = SerializableFactory.getJackson();
                        NodeHeartbeatInfo deserializable = jackson.deserializable(heartInfo.getBytes(StandardCharsets.UTF_8), NodeHeartbeatInfo.class);
                        node.setNodeHeartbeatInfo(deserializable);
                    }
                    return node;
                } catch (Exception e2) {
                    log.error("get nodes appear exception", e2);
                    return null;
                }
            }).filter(Objects::nonNull).toList();
        });
    }

    @Override
    public void invalidHost(Host host) {
        ConcurrentMap<String, List<Node>> map = HOST_CACHE.asMap();
        for (String key : map.keySet()) {
            List<Node> nodes = map.get(key);
            Iterator<Node> iterator = nodes.iterator();
            while (iterator.hasNext()){
                Node next = iterator.next();
                Host host1 = next.getHost();
                if(host.equals(host1)){
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public Node selectNodeByServerName(String serverName, LbType lbType) {
        return null;
    }

    @Override
    public void startHeartbeat() {

    }
}
