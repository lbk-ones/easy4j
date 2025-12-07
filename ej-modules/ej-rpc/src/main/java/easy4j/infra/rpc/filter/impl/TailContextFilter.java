package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

import static easy4j.infra.rpc.domain.RpcResponse.ERROR_MSG_ID;

/**
 * 最后一个过滤器
 */
public class TailContextFilter implements RpcFilter {
    @Override
    public void doFilter(RpcFilterContext context, RpcFilterChain chain) {
        RpcFilterContext.ExecutorSide executorSide = context.getExecutorSide();
        RpcFilterContext.ExecutorPhase executorPhase = context.getExecutorPhase();
        if (executorSide == RpcFilterContext.ExecutorSide.CLIENT && executorPhase == RpcFilterContext.ExecutorPhase.REQUEST_BEFORE) {
            RpcRequest rpcRequest = context.getRpcRequest();
            if (rpcRequest == null) {
                context.setRpcResponse(RpcResponse.error(ERROR_MSG_ID, RpcResponseStatus.PARAM_MISSING, "rpcRequest is not null"));
                context.interrupted();
            }
        }
    }
}
