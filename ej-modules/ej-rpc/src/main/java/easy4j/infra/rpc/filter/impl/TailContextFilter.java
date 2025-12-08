package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.ExecutorPhase;
import easy4j.infra.rpc.enums.ExecutorSide;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.filter.Activate;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

import static easy4j.infra.rpc.domain.RpcResponse.ERROR_MSG_ID;

/**
 * 最后一个过滤器
 * 校验 rpcRequest是否为空，不允许为空
 */
@Activate(group = ExecutorSide.ALL)
public class TailContextFilter implements RpcFilter {
    @Override
    public void invoke(RpcFilterContext context, RpcFilterChain chain, ExecutorSide executorSide) {
        RpcRequest rpcRequest = context.getRpcRequest();
        if (rpcRequest == null) {
            context.setRpcResponse(RpcResponse.error(ERROR_MSG_ID, RpcResponseStatus.PARAM_MISSING, "rpcRequest is not null"));
            // interrupted
            context.interrupted();
        }
        // will be skip
        chain.invoke(context);
    }

    @Override
    public void onResponse(RpcFilterContext context, RpcResponse rpcResponse, ExecutorSide executorSide) {

        if (rpcResponse != null) {
            int code = rpcResponse.getCode();
            RpcResponseStatus byCode = RpcResponseStatus.getByCode(code);
            if (!byCode.isSuccess()) {

            }
        }

    }
}
