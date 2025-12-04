package easy4j.infra.rpc.server;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.enums.LbType;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.heart.NodeHeartbeatInfo;
import easy4j.infra.rpc.heart.NodeHeartbeatManager;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.registry.Event;
import easy4j.infra.rpc.registry.Registry;
import easy4j.infra.rpc.registry.RegistryFactory;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.utils.DefaultUncaughtExceptionHandler;
import easy4j.infra.rpc.utils.Host;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Collectors;

/**
 * 默认节点实现
 */
@Slf4j
public class DefaultServerNode implements ServerNode {

    private static final Cache<String, List<Node>> HOST_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES) // 写入后 5 分钟过期
            .maximumSize(10_000) // 最大缓存容量 10000 条（超量后触发淘汰）
            .build();

    private static final Map<String, Integer> roundRobin = new ConcurrentHashMap<>();

    private static final Map<String, WeightedRoundRobinScheduler> roundRobinWeight = new ConcurrentHashMap<>();

    // 饿汉
    public static final DefaultServerNode INSTANCE = new DefaultServerNode();

    private final Registry registry;
    public static final ThreadFactory registerThreadFactory = new NamedThreadFactory("e4j-register-heart-thread-", null, true, DefaultUncaughtExceptionHandler.getInstance());

    private static final Map<String, Boolean> registerMap = new ConcurrentHashMap<>();

    private DefaultServerNode() {
        this.registry = RegistryFactory.get();
    }

    /**
     * Retrieve all host information based on service name and cache it <br/>
     * If the value is not obtained from the registration center, do not subscribe to updates. If it is obtained, subscribe to changes in host information
     *
     * @param serverName server name
     * @return List<Node>
     * @author bokun.li
     */
    @Override
    public List<Node> getNodesByServerName(String serverName) {
        if (StrUtil.isBlank(serverName)) return new ArrayList<>();
        String s = RegisterInfoType.NODE.getRegisterPath() + StrPool.SLASH + serverName;
        List<Node> nodes = HOST_CACHE.get(serverName, sn -> {
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
                        // 如果注册中心直接禁用了 那么就过滤掉它
                        if (deserializable.isDisabled()) {
                            return null;
                        }
                    }
                    return node;
                } catch (Exception e2) {
                    log.error("get nodes appear exception", e2);
                    return null;
                }
            }).filter(Objects::nonNull).toList();
        });
        nodes = nodes.stream().filter(Node::isEnabled).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(nodes)) {
            roundRobinWeight.putIfAbsent(serverName, new WeightedRoundRobinScheduler(nodes));
            subscribe(serverName, s);
        }
        return nodes;
    }

    /**
     * Changes in Subscription Service Hosts
     *
     * @param serverName Server name
     * @param s          The path to subscribe to
     */
    private synchronized void subscribe(String serverName, String s) {
        if (!registry.isSubscribe(s)) {
            registry.subscribe(s, e -> {
                WeightedRoundRobinScheduler weightedRoundRobinScheduler = roundRobinWeight.get(serverName);
                String data = e.data();
                Event.Type type = e.type();
                String path = e.path();
                if (StrUtil.isBlank(path)) return;
                Host newHost;
                try {
                    String substring = path.substring(s.length() + 1);
                    String address = substring.substring(0, substring.indexOf(StrPool.SLASH));
                    String[] split = address.split(StrPool.COLON);
                    String s1 = split[0];
                    String s2 = split[1];
                    newHost = new Host(s1, Integer.parseInt(s2));
                } catch (Exception ex) {
                    return;
                }
                switch (type) {
                    case ADD -> {
                        List<Node> nodes1 = HOST_CACHE.get(serverName, (e2) -> new CopyOnWriteArrayList<>());
                        Node node = new Node(newHost, true);
                        if (StrUtil.isNotEmpty(data)) {
                            ISerializable jackson = SerializableFactory.getJackson();
                            NodeHeartbeatInfo deserializable = jackson.deserializable(data.getBytes(StandardCharsets.UTF_8), NodeHeartbeatInfo.class);
                            node.setNodeHeartbeatInfo(deserializable);
                        }
                        nodes1.add(node);
                        weightedRoundRobinScheduler.addNode(node);
                    }
                    case UPDATE -> {
                        List<Node> nodes1 = HOST_CACHE.get(serverName, (e2) -> new CopyOnWriteArrayList<>());
                        NodeHeartbeatInfo deserializable = null;
                        if (StrUtil.isNotEmpty(data)) {
                            ISerializable jackson = SerializableFactory.getJackson();
                            deserializable = jackson.deserializable(data.getBytes(StandardCharsets.UTF_8), NodeHeartbeatInfo.class);
                        }
                        for (Node node : nodes1) {
                            Host host = node.getHost();
                            if (newHost.equals(host) && deserializable != null) {
                                node.setNodeHeartbeatInfo(deserializable);
                                weightedRoundRobinScheduler.updateWeight(newHost, deserializable.getWeight());
                            }
                        }
                    }
                    case REMOVE -> {
                        List<Node> nodes1 = HOST_CACHE.get(serverName, (e2) -> new CopyOnWriteArrayList<>());
                        Iterator<Node> iterator = nodes1.iterator();
                        while (iterator.hasNext()) {
                            Node next = iterator.next();
                            Host host = next.getHost();
                            if (host.equals(newHost)) {
                                weightedRoundRobinScheduler.removeNode(host);
                                iterator.remove();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void invalidHost(Host host) {
        ConcurrentMap<String, List<Node>> map = HOST_CACHE.asMap();
        for (String key : map.keySet()) {
            List<Node> nodes = map.get(key);
            Iterator<Node> iterator = nodes.iterator();
            while (iterator.hasNext()) {
                Node next = iterator.next();
                Host host1 = next.getHost();
                if (host.equals(host1)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public Node selectNodeByServerName(String serverName, LbType lbType) {
        List<Node> nodesByServerName = getNodesByServerName(serverName);
        if (CollUtil.isEmpty(nodesByServerName)) return null;
        return switch (lbType) {
            case ROUND_ROBIN -> {
                // 轮询中简单用下权重,权重高的多轮询几次
                List<Node> nodesNew = extractByWeight(nodesByServerName);
                Integer i = roundRobin.get(serverName);
                if (i == null || i >= (nodesNew.size() - 1)) {
                    roundRobin.put(serverName, 0);
                    yield nodesNew.get(0);
                } else {
                    i++;
                    int min = Math.min(i, nodesNew.size() - 1);
                    roundRobin.put(serverName, min);
                    yield nodesNew.get(min);
                }
            }
            case WEIGHT_ROUND_BING, PERFORMANCE_BASED -> {
                WeightedRoundRobinScheduler weightedRoundRobinScheduler = roundRobinWeight.get(serverName);
                yield weightedRoundRobinScheduler.select();
            }
            case RANDOM -> {
                int i = RandomUtil.randomInt(nodesByServerName.size());
                yield nodesByServerName.get(i);
            }
            case LEAST_CONNECTIONS -> {
                // has delay
                nodesByServerName.sort((o1, o2) -> {
                    Integer conn1 = o1.getNodeHeartbeatInfo().getConn();
                    Integer conn2 = o2.getNodeHeartbeatInfo().getConn();
                    return conn2.compareTo(conn1);
                });
                yield nodesByServerName.get(0);
            }
        };
    }

    private List<Node> extractByWeight(List<Node> nodesByServerName) {
        List<Node> nodesNew = new ArrayList<>();
        for (Node node : nodesByServerName) {
            NodeHeartbeatInfo nodeHeartbeatInfo = node.getNodeHeartbeatInfo();
            int weight = nodeHeartbeatInfo.getWeight();
            if (weight > 0) {
                for (int i = 0; i < weight; i++) {
                    Node clone = node.clone();
                    nodesNew.add(clone);
                }
            } else {
                nodesNew.add(node);
            }
        }
        return nodesNew;
    }


    @Override
    public void registry(NodeHeartbeatManager nodeHeartbeatManager, String serviceName) {

        if (nodeHeartbeatManager == null) return;
        if (registerMap.get(serviceName) == null) {
            registerMap.put(serviceName, true);
            registerWith(nodeHeartbeatManager, serviceName);
        }
    }

    private void registerWith(NodeHeartbeatManager nodeHeartbeatManager, String serviceName) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(registerThreadFactory);
        E4jRpcConfig config = IntegratedFactory.getRpcConfig().getConfig();
        Long heartInfoReportFixRateMilli = config.getServer().getHeartInfoReportFixRateMilli();

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            private boolean exeIng = false;

            private int num = 0;

            @Override
            public void run() {
                if (exeIng) return;
                exeIng = true;
                try {
                    NodeHeartbeatInfo nodeHeartbeatInfo = nodeHeartbeatManager.buildHeart();
                    String s = RegisterInfoType.NODE.getRegisterPath() + StrPool.SLASH + serviceName + StrPool.SLASH + nodeHeartbeatInfo.getHost() + StrPool.COLON + nodeHeartbeatInfo.getPort();
                    ISerializable jackson = SerializableFactory.getJackson();
                    registry.put(s, new String(jackson.serializable(nodeHeartbeatInfo), StandardCharsets.UTF_8), true);
                    if (num == 0) {
                        log.info("e4j server {} registry success!", serviceName);
                    }
                    num++;
                } finally {
                    exeIng = false;
                }
            }
        }, 0, heartInfoReportFixRateMilli, TimeUnit.MILLISECONDS);
    }
}
