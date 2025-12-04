package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;

/**
 * 泛化调用
 * <pre>
 *
 * </pre>
 */
public interface GeneralizedInvoke {


    RpcResponse invoke(RpcRequest request);


}
