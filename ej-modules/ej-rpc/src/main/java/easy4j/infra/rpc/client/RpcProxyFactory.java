package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;

import java.lang.reflect.Proxy;

/**
 * 客户端 rpc 代理类
 */
public class RpcProxyFactory {

    public static <T> T getProxy(Class<T> tClass) {
        Object o = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, (proxy, method, args) -> {
            RpcRequest rpcRequest = RpcRequest.of(method, args);
            RpcResponse rpcResponse = RpcClientFactory.getClient().sendRequestSync(rpcRequest);
            return rpcResponse.getResult();
        });
        return (T) o;
    }
}
