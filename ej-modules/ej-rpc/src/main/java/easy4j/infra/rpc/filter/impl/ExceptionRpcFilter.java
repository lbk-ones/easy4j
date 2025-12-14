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
 * 只处理客户端的异常，因为服务端异常自己处理完会发送会客户端
 * 如果过滤链被提前中断那么这里就不会被回调
 */
@Activate(group = ExecutorSide.CLIENT)
public class ExceptionRpcFilter implements RpcFilter {

    @Override
    public void invoke(RpcFilterContext context, RpcFilterChain chain, ExecutorSide executorSide) {
        chain.invoke(context);
    }

    /**
     * 调用结果如果错误
     * 几种错误:
     * 1、RpcFilter出现的异常
     * 2、RpcFilter执行完之后调用时的异常
     * 3、rpc客户端调用返回的服务端异常
     * 先处理前面的两种错误，然后再处理第三种错误
     *
     * @param context      上下问
     * @param rpcResponse  返回结果 可能为null
     * @param executorSide 执行端
     */
    @Override
    public void onResponse(RpcFilterContext context, RpcResponse rpcResponse, ExecutorSide executorSide) {
        if (rpcResponse != null) {
            Throwable unknownException = rpcResponse.getUnknownException();
            if (unknownException != null) {
                throw new RpcException(unknownException);
            }
            int code = rpcResponse.getCode();
            RpcResponseStatus byCode = RpcResponseStatus.getByCode(code);
            if (!byCode.isSuccess()) {
                throw new RpcException(rpcResponse.getMessage());
            }
        }
    }

    @Override
    public Integer getPriority() {
        return Integer.MAX_VALUE;
    }
}
