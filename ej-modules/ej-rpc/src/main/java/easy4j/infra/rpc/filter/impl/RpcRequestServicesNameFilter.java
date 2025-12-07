package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.domain.ProxyAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

import java.util.Map;

/**
 * 处理serviceName的值
 */
public class RpcRequestServicesNameFilter implements RpcFilter {
    @Override
    public void doFilter(RpcFilterContext context, RpcFilterChain chain) {
        RpcFilterContext.ExecutorSide executorSide = context.getExecutorSide();
        RpcFilterContext.ExecutorPhase executorPhase = context.getExecutorPhase();
        if (executorSide == RpcFilterContext.ExecutorSide.CLIENT && executorPhase == RpcFilterContext.ExecutorPhase.REQUEST_BEFORE) {
            ProxyAttributes proxyAttributes = context.getProxyAttributes();
            if (proxyAttributes != null) {
                RpcRequest rpcRequest = context.getRpcRequest();
                rpcRequest.setServiceName(proxyAttributes.getServiceName());
            }
        }
    }
}
