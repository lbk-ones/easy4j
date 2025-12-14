package easy4j.infra.rpc.server.serverhandlers;

import easy4j.infra.rpc.config.CommonConstant;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.registry.ServiceControl;
import easy4j.infra.rpc.registry.jdbc.ServiceManagement;
import easy4j.infra.rpc.server.ServerHandler;
import easy4j.infra.rpc.utils.Host;
import lombok.extern.slf4j.Slf4j;

/**
 * 刷新缓存
 *
 * @since 2.0.1
 */
@Slf4j
public class FlushCacheHandlers implements ServerHandler {

    @Override
    public boolean support(RpcRequest request, Transport transport) {
        return request.matchHandler(CommonConstant.SERVER_FLUSH_CACHE);
    }

    @Override
    public RpcResponse handler(RpcRequest request, Transport transport) {
        try {
            ServiceManagement parametersIndex = request.takeParameterByIndex(0, ServiceManagement.class);
            if (parametersIndex != null) {
                String host = parametersIndex.getHost();
                int port = parametersIndex.getPort();
                String key = ServiceControl.INSTANCE.getServiceKey(parametersIndex.getServiceName(), new Host(host, port));
                ServiceControl.INSTANCE.setCache(key, parametersIndex);
            }
            return RpcResponse.success(transport.getMsgId());
        } catch (Exception e) {
            log.error("server side flush cache handler appear error ", e);
            return RpcResponse.error(transport.getMsgId(), RpcResponseStatus.BUSINESS_ERROR, "cache flush error!" + e.getMessage());
        }

    }
}
