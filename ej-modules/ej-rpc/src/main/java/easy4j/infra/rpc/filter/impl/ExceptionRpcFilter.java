package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.ExecutorSide;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.filter.Activate;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

/**
 * 抛出rpcException
 */
@Activate(group = ExecutorSide.CLIENT)
public class ExceptionRpcFilter implements RpcFilter {

    @Override
    public void invoke(RpcFilterContext context, RpcFilterChain chain, ExecutorSide executorSide) {
        Throwable exception = context.getException();
        if (exception != null) {
            throw new RpcException("appear exception ", exception);
        } else {
            chain.invoke(context);
        }
    }

    /**
     * 调用结果如果错误，则抛出异常
     * @param context      上下问
     * @param rpcResponse  返回结果 可能为null
     * @param executorSide 执行端
     */
    @Override
    public void onResponse(RpcFilterContext context, RpcResponse rpcResponse, ExecutorSide executorSide) {
        if (rpcResponse != null) {
            int code = rpcResponse.getCode();
            RpcResponseStatus byCode = RpcResponseStatus.getByCode(code);
            if (!byCode.isSuccess()) {
                throw new RpcException(rpcResponse.getMessage());
            }
        }
    }
}
