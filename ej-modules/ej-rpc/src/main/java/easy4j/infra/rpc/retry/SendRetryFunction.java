package easy4j.infra.rpc.retry;

import easy4j.infra.rpc.domain.RpcResponse;

public interface SendRetryFunction {
    RpcResponse retry();
}