package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.ProxyAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.filter.*;
import easy4j.infra.rpc.filter.impl.TailContextFilter;
import easy4j.infra.rpc.utils.Host;
import easy4j.infra.rpc.utils.SpiUtils;

import java.util.List;

/**
 * 客户端包装器
 *
 * @author bokun
 * @since 2.0.1
 */
public class RpcClientWrapper {

    final RpcFilterChain rpcFilterChain;
    final RpcClient rpcClient;

    public RpcClientWrapper(RpcClient rpcClient) {
        List<RpcFilter> load = SpiUtils.load(RpcFilter.class);
        load.add(new TailContextFilter());
        this.rpcFilterChain = RpcFilterChain.build(load);
        this.rpcClient = rpcClient;
    }

    public RpcResponse sendRequestSync(RpcRequest rpcRequest, ProxyAttributes proxyAttributes) {
        return sendRequestSync(rpcRequest, null, proxyAttributes);
    }

    public RpcResponse sendRequestSync(RpcRequest rpcRequest, Host host, ProxyAttributes proxyAttributes) {
        RpcFilterContext rpcContext = new DefaultRpcFilterContext();
        rpcContext.setExecutorSide(RpcFilterContext.ExecutorSide.CLIENT);
        rpcContext.setExecutorPhase(RpcFilterContext.ExecutorPhase.REQUEST_BEFORE);
        rpcContext.setRpcRequest(rpcRequest);
        rpcContext.setProxyAttributes(proxyAttributes);
        this.rpcFilterChain.doFilter(rpcContext);
        if (rpcContext.isInterrupted()) {
            // 链路中断，返回错误响应
            return rpcContext.getRpcResponse();
        }
        RpcRequest rpcRequest1 = rpcContext.getRpcRequest();
        RpcResponse rpcResponse;
        if (host == null) {
            rpcResponse = rpcClient.sendRequestSync(rpcRequest1);
        } else {
            rpcResponse = rpcClient.sendRequestSync(rpcRequest1, host);
        }
        rpcContext.setRpcResponse(rpcResponse);
        rpcContext.setExecutorPhase(RpcFilterContext.ExecutorPhase.RESPONSE_BEFORE);
        this.rpcFilterChain.doFilter(rpcContext);
        return rpcContext.getRpcResponse();


    }
}
