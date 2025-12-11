package easy4j.infra.rpc.enums;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.exception.RpcException;

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
    LEAST_CONNECTIONS;

    public static LbType of(String name){
        LbType[] values = LbType.values();
        for (LbType value : values) {
            String name1 = value.name();
            if(StrUtil.equals(name1,name)){
                return value;
            }
        }
        throw new RpcException("not support the LoadBlance type "+name);
    }
}
