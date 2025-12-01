package easy4j.infra.rpc.enums;

public enum LbType {

    /**
     * 轮询
     */
    ROUND_ROBIN,

    /**
     * 加权轮询
     */
    WEIGHT_ROUND_BING,

    /**
     * 随机
     */
    RANDOM,

    /**
     * 性能最优
     */
    PERFORMANCE_BASED,

    /**
     * 最小连接数
     */
    LEAST_CONNECTIONS
}
