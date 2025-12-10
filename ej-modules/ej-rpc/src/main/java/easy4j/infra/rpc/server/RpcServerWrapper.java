package easy4j.infra.rpc.server;

import easy4j.infra.rpc.config.CommonConstant;
import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.ExecutorPhase;
import easy4j.infra.rpc.enums.ExecutorSide;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.filter.*;

/**
 * 客户端包装器
 *
 * @author bokun
 * @since 2.0.1
 */
public class RpcServerWrapper extends AbstractRpcWrapper {

    RpcFilterChain rpcFilterChain;
    final ServerMethodInvoke serverMethodInvoke;


    public RpcServerWrapper(ServerMethodInvoke serverMethodInvoke) {
        this.serverMethodInvoke = serverMethodInvoke;
    }

    public RpcResponse invoke() {
        RpcRequest request = serverMethodInvoke.getRequest();
        Transport transport = serverMethodInvoke.getTransport();
        RpcFilterContext rpcContext = new DefaultRpcFilterContext();
        rpcContext.setExecutorSide(ExecutorSide.SERVER);
        rpcContext.setExecutorPhase(ExecutorPhase.REQUEST_BEFORE);
        rpcContext.setRpcRequest(request);
        FilterAttributes filterAttributes = new FilterAttributes();
        filterAttributes.setServiceName(request.getServiceName());
        boolean generalizedInvoke = "true".equals(request.getAttachment().get(CommonConstant.IS_GENERALIZED_INVOKE));
        filterAttributes.setGeneralizedInvoke(generalizedInvoke);
        filterAttributes.setTransport(transport);
        rpcContext.setFilterAttributes(filterAttributes);
        RpcFilterNode firstFilterNode = getFirstFilterNode(rpcContext);
        this.rpcFilterChain = RpcFilterChain.build(firstFilterNode);
        this.rpcFilterChain.invoke(rpcContext);
        if (rpcContext.isInterrupted() && rpcContext.getRpcResponse() != null) {
            // 链路中断，返回错误响应
            return rpcContext.getRpcResponse();
        }
        Throwable exception = rpcContext.getException();
        RpcResponse invoke;
        if (exception == null) {
            invoke = serverMethodInvoke.invoke();
        } else {
            invoke = RpcResponse.error(RpcResponse.ERROR_MSG_ID, RpcResponseStatus.SERVER_ERROR);
            invoke.setUnknownException(exception);
        }
        // reset
        rpcContext.setRpcResponse(invoke);
        rpcContext.setExecutorPhase(ExecutorPhase.RESPONSE_BEFORE);
        rpcContext.setException(null);
        this.rpcFilterChain.reset(firstFilterNode);
        this.rpcFilterChain.invoke(rpcContext);
        return rpcContext.getRpcResponse();
    }
}
