package easy4j.infra.rpc.client;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.ExecutorPhase;
import easy4j.infra.rpc.enums.ExecutorSide;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.filter.*;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.utils.Host;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 客户端包装器
 *
 * @author bokun
 * @since 2.0.1
 */
public class RpcClientWrapper extends AbstractRpcWrapper {

    RpcFilterChain rpcFilterChain;
    final RpcClient rpcClient;

    public RpcClientWrapper(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public RpcResponse sendRequestSync(RpcRequest rpcRequest, FilterAttributes filterAttributes) {
        return sendRequestSync(rpcRequest, determineHost(rpcRequest, filterAttributes), filterAttributes);
    }

    public RpcResponse sendRequestSync(RpcRequest rpcRequest, Host host, FilterAttributes filterAttributes) {
        RpcFilterContext rpcContext = new DefaultRpcFilterContext();
        rpcContext.setExecutorSide(ExecutorSide.CLIENT);
        rpcContext.setExecutorPhase(ExecutorPhase.REQUEST_BEFORE);
        rpcContext.setRpcRequest(rpcRequest);
        rpcContext.setFilterAttributes(filterAttributes);
        RpcFilterNode firstFilterNode = getFirstFilterNode(rpcContext);
        this.rpcFilterChain = RpcFilterChain.build(firstFilterNode);
        this.rpcFilterChain.invoke(rpcContext);
        if (rpcContext.isInterrupted() && rpcContext.getRpcResponse() != null) {
            // 链路中断，提前返回
            return rpcContext.getRpcResponse();
        }
        Throwable exception = rpcContext.getException();
        RpcRequest rpcRequest1 = rpcContext.getRpcRequest();
        RpcResponse rpcResponse = RpcResponse.error(RpcResponse.ERROR_MSG_ID, RpcResponseStatus.CLIENT_ERROR);
        // 如果CLIENT的REQUEST_BEFORE阶段出现了异常、那么不会执行调用请求，但是还是要过RESPONSE_BEFORE阶段的Filter逻辑
        if (exception != null) {
            try {
                if (host == null) {
                    if (!rpcClient.sendRequestBroadCast(rpcRequest1)) {
                        // SLB
                        rpcResponse = rpcClient.sendRequestSync(rpcRequest1);
                    }
                } else {
                    rpcResponse = rpcClient.sendRequestSync(rpcRequest1, host);
                }
                rpcResponse.setUnknownException(null);
            } catch (Throwable throwable) {
                rpcResponse.setMessage(throwable.getMessage());
                rpcResponse.setUnknownException(throwable);
            }
        } else {
            rpcResponse.setUnknownException(exception);
        }
        rpcContext.setRpcResponse(rpcResponse);
        rpcContext.setExecutorPhase(ExecutorPhase.RESPONSE_BEFORE);
        rpcContext.setException(null);
        // reset
        this.rpcFilterChain.reset(firstFilterNode);
        this.rpcFilterChain.invoke(rpcContext);
        return rpcContext.getRpcResponse();
    }

    /**
     * 决断host
     *
     * @param rpcRequest       请求信息
     * @param filterAttributes 拦截器信息
     * @return easy4j.infra.rpc.utils.Host
     */
    private static Host determineHost(RpcRequest rpcRequest, FilterAttributes filterAttributes) {
        String url = filterAttributes.getUrl();
        Host host = null;
        if (StrUtil.isNotBlank(url)) host = new Host(url);
        if (host == null) {
            Map<String, E4jRpcConfig.ReferenceUrl> reference = IntegratedFactory.getConfig().getReference();
            String serviceName = rpcRequest.getServiceName();
            host = Optional.ofNullable(serviceName)
                    .map(e -> reference)
                    .map(e -> e.get(serviceName))
                    .map(E4jRpcConfig.ReferenceUrl::getUrl)
                    .map(e -> {
                        try {
                            return new Host(e);
                        } catch (Exception ex) {
                            return null;
                        }
                    })
                    .orElse(null);
        }
        return host;
    }
}
