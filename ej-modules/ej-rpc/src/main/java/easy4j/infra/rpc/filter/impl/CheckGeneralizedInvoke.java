package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.domain.ProxyAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

/**
 * 处理泛化调用的参数
 */
public class CheckGeneralizedInvoke implements RpcFilter {
    @Override
    public void doFilter(RpcFilterContext context, RpcFilterChain chain) {
        RpcFilterContext.ExecutorSide executorSide = context.getExecutorSide();
        RpcFilterContext.ExecutorPhase executorPhase = context.getExecutorPhase();
        if (executorSide == RpcFilterContext.ExecutorSide.CLIENT && executorPhase == RpcFilterContext.ExecutorPhase.REQUEST_BEFORE) {
            ProxyAttributes proxyAttributes = context.getProxyAttributes();
            if (proxyAttributes != null && proxyAttributes.isGeneralizedInvoke()) {
                Object[] proxyMethodArgs = proxyAttributes.getProxyMethodArgs();
                if (proxyMethodArgs == null || proxyMethodArgs.length == 0) {
                    context.setRpcResponse(RpcResponse.error(0, RpcResponseStatus.PARAM_MISSING));
                    context.interrupted();
                    return;
                }
                Object arg = proxyAttributes.getProxyMethodArgs()[0];
                if (arg == null) {
                    context.setRpcResponse(RpcResponse.error(0, RpcResponseStatus.PARAM_MISSING));
                    context.interrupted();
                    return;
                }
                RpcRequest arg1 = (RpcRequest) arg;
                context.setRpcRequest(arg1);
            }
        }
        chain.doFilter(context);
    }
}
