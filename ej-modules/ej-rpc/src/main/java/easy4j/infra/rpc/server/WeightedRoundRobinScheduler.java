package easy4j.infra.rpc.server;

import easy4j.infra.rpc.utils.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 平滑加权轮询调度器（线程安全、支持动态权重）
 */
public class WeightedRoundRobinScheduler {
    // 服务节点列表（线程安全，支持动态增减）
    private final CopyOnWriteArrayList<Node> nodes = new CopyOnWriteArrayList<>();
    // 每个节点的当前权重（与 nodes 索引一一对应，原子操作保证线程安全）
    private AtomicIntegerArray currentWeights;
    // 总权重（缓存，避免每次计算）
    private volatile int totalWeight;
    // 锁：保证 currentWeights 更新的原子性（多步操作需同步）
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 初始化节点列表
     */
    public WeightedRoundRobinScheduler(List<Node> initialNodes) {
        if (initialNodes == null || initialNodes.isEmpty()) {
            throw new IllegalArgumentException("The initial node list cannot be empty!");
        }
        nodes.addAll(initialNodes);
        resetCurrentWeights();
        calculateTotalWeight();
    }

    /**
     * 重置当前权重（初始化/节点变化时调用）
     */
    private void resetCurrentWeights() {
        currentWeights = new AtomicIntegerArray(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            currentWeights.set(i, 0);
        }
    }

    /**
     * 计算总权重（过滤不健康节点和权重<=0的节点）
     */
    private void calculateTotalWeight() {
        totalWeight = nodes.stream()
                .filter(Node::isEnabled)
                .filter(e -> e.getNodeHeartbeatInfo() != null)
                .mapToInt(e -> e.getNodeHeartbeatInfo().getWeight())
                .sum();
    }

    /**
     * 核心：选择下一个服务节点
     */
    public Node select() {
        lock.lock();
        try {
            // 过滤可用节点（健康且权重>0）
            List<Integer> availableIndexes = new ArrayList<>();
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                if (node.isEnabled() && node.getNodeHeartbeatInfo() != null && node.getNodeHeartbeatInfo().getWeight() > 0) {
                    availableIndexes.add(i);
                }
            }

            if (availableIndexes.isEmpty()) {
                return null;
            }

            // 步骤1：所有可用节点的 currentWeight += 自身权重
            int maxWeight = -1;
            int selectedIndex = -1;
            for (int idx : availableIndexes) {
                Node node = nodes.get(idx);
                int newWeight = currentWeights.get(idx) + node.getNodeHeartbeatInfo().getWeight();
                currentWeights.set(idx, newWeight);

                // 步骤2：记录当前权重最大的节点索引
                if (newWeight > maxWeight) {
                    maxWeight = newWeight;
                    selectedIndex = idx;
                }
            }

            // 步骤3：选中节点的 currentWeight -= 总权重（重置）
            currentWeights.set(selectedIndex, maxWeight - totalWeight);

            return nodes.get(selectedIndex);
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------ 扩展功能 ------------------------------

    /**
     * 动态添加节点
     */
    public void addNode(Node node) {
        lock.lock();
        try {
            nodes.add(node);
            resetCurrentWeights(); // 重新初始化当前权重数组
            calculateTotalWeight();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 动态移除节点
     */
    public void removeNode(Host host) {
        lock.lock();
        try {
            nodes.removeIf(node -> host.equals(node.getHost()));
            resetCurrentWeights();
            calculateTotalWeight();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 动态修改节点权重
     */
    public void updateWeight(Host address, int newWeight) {
        lock.lock();
        try {
            for (Node node : nodes) {
                if (address.equals(node.getHost())) {
                    node.getNodeHeartbeatInfo().setWeight(newWeight);
                    calculateTotalWeight();
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新节点健康状态
     */
    public void updateHealthy(Host host, boolean healthy) {
        lock.lock();
        try {
            for (Node node : nodes) {
                if (host.equals(node.getHost())) {
                    node.setEnabled(healthy);
                    calculateTotalWeight(); // 健康状态变化可能影响总权重
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }
}