package easy4j.infra.rpc.server;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;

public interface ServerHandler {

    boolean support(RpcRequest request, Transport transport);

    RpcResponse handler(RpcRequest request, Transport transport);

    /**
     * 过滤器优先级（数值越小，执行顺序越靠前）
     */
    default Integer getPriority() {
        return 0;
    }

}
