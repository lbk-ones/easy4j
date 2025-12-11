package easy4j.infra.rpc.registry.jdbc;

import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.enums.RetryType;
import easy4j.infra.rpc.heart.NodeHeartbeatManager;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.registry.ServiceControl;
import easy4j.infra.rpc.utils.Host;
import lombok.Data;

import java.io.Serializable;
import java.util.function.Function;
/**
 * 服务治理
 */
@Data
public class ServiceManagement implements Serializable,Cloneable {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 是否禁用这个服务，如果禁用那么就不会路由到这个服务来
     */
    private Boolean disabled = false;

    /**
     * 调用超时时间 milliseconds
     */
    private Long invokeTimeOutMillis;

    /**
     * 负载均衡算法
     */
    private String lbType;

    /**
     * qps限制
     */
    private Integer qps = -1;

    /**
     * 权重
     */
    private Integer weight = 1;

    /**
     * 最大重试次数 默认三次
     */
    private Integer invokeRetryMaxCount = 3;

    /**
     * 重试类型，1、阻塞
     */
    private RetryType retryType;

    /**
     * 兼容获取值
     * @param function          服务治理处理器
     * @param supplier          配置
     * @param <T>               泛型
     * @return 泛型值
     */
    public <T> T get( Function<ServiceManagement, T> function, Function<E4jRpcConfig,T> supplier) {
        T apply = function.apply(this);
        if (apply == null) {
            E4jRpcConfig config = IntegratedFactory.getConfig();
            return supplier.apply(config);
        } else {
            return apply;
        }
    }

    public static <T> T getCurrent( Function<ServiceManagement, T> function, Function<E4jRpcConfig,T> supplier) {
        ServiceManagement currentServiceManagement = getCurrentServiceManagement();
        return currentServiceManagement.get(function,supplier);
    }

    public static ServiceManagement getCurrentServiceManagement(){
        return ServiceControl.INSTANCE.get(
                IntegratedFactory
                        .getConfig()
                        .getServer()
                        .getServerName(),
                new Host(RpcRequest.CurrentIpHolder.CURRENT_IP, NodeHeartbeatManager.port)
        );
    }

    public static ServiceManagement getServiceManagement(String serviceName,Host host){
        return ServiceControl.INSTANCE.get(
                serviceName,
                host
        );
    }

    @Override
    public ServiceManagement clone() {
        try {
            return (ServiceManagement) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
