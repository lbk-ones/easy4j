package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.enums.ExecutorSide;
import easy4j.infra.rpc.filter.Activate;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

/**
 * 处理serviceName的值
 * 从FilterAttributes拿取服务名，客户端调用的时候很关键
 */
@Activate(group = ExecutorSide.CLIENT)
public class RpcRequestServicesNameFilter implements RpcFilter {
    @Override
    public void invoke(RpcFilterContext context, RpcFilterChain chain, ExecutorSide executorSide) {
        FilterAttributes filterAttributes = context.getFilterAttributes();
        if (filterAttributes != null) {
            RpcRequest rpcRequest = context.getRpcRequest();
            rpcRequest.setServiceName(filterAttributes.getServiceName());
        }
        chain.invoke(context);
    }
}
