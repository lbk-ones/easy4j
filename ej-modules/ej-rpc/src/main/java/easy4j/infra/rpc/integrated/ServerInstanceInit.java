package easy4j.infra.rpc.integrated;

import easy4j.infra.rpc.domain.RpcRequest;

/**
 * 服务端实例获取方式
 *
 * @author bokun
 * @since 2.0.1
 */
public interface ServerInstanceInit {


    /**
     * 获取实例
     * @param request 请求体
     * @return
     */
    Object instance(RpcRequest request);


}
