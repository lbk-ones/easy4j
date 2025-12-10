package easy4j.infra.rpc.registry.jdbc;

import easy4j.infra.rpc.enums.LbType;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务治理
 */
@Data
public class ServiceManagement implements Serializable {

    /**
     * 是否禁用这个服务，如果禁用那么就不会路由到这个服务来
     */
    private boolean disabled;

    /**
     * 调用超时时间
     */
    private String timeout;

    /**
     * 负载均衡算法
     */
    private LbType lbType;

    /**
     * qps限制
     */
    private int qps;

}
