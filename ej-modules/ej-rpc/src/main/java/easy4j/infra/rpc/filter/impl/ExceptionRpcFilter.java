package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

/**
 * 抛出rpcException
 */
public class ExceptionRpcFilter implements RpcFilter {

    @Override
    public void doFilter(RpcFilterContext context, RpcFilterChain chain) {
        RpcFilterContext.ExecutorSide executorSide = context.getExecutorSide();
        RpcFilterContext.ExecutorPhase executorPhase = context.getExecutorPhase();
        if (executorSide == RpcFilterContext.ExecutorSide.CLIENT && executorPhase == RpcFilterContext.ExecutorPhase.REQUEST_BEFORE) {
            Throwable exception = context.getException();
            if (exception != null) {
                throw new RuntimeException("appear exception ", exception);
            } else {
                chain.doFilter(context);
            }
        }
    }
}
