package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.ExecutorSide;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.filter.Activate;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

/**
 * 处理泛化调用的参数
 */
@Activate(group = ExecutorSide.CLIENT)
public class CheckGeneralizedInvoke implements RpcFilter {
    @Override
    public void invoke(RpcFilterContext context, RpcFilterChain chain, ExecutorSide executorSide) {
        FilterAttributes filterAttributes = context.getFilterAttributes();
        if (filterAttributes != null && filterAttributes.isGeneralizedInvoke()) {
            Object[] proxyMethodArgs = filterAttributes.getProxyMethodArgs();
            if (proxyMethodArgs == null || proxyMethodArgs.length == 0) {
                context.setRpcResponse(RpcResponse.error(0, RpcResponseStatus.PARAM_MISSING));
                context.interrupted();
                return;
            }
            Object arg = filterAttributes.getProxyMethodArgs()[0];
            if (arg == null) {
                context.setRpcResponse(RpcResponse.error(0, RpcResponseStatus.PARAM_MISSING));
                context.interrupted();
                return;
            }
            RpcRequest arg1 = (RpcRequest) arg;
            context.setRpcRequest(arg1);
        }
        chain.invoke(context);
    }
}
