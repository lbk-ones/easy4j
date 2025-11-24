package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import java.lang.reflect.Proxy;

public class RpcProxyFactory {

    public static <T> T getProxy(Class<T> tClass){
        Object o = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, (proxy, method, args) -> {
            RpcRequest rpcRequest = RpcRequest.of(method, args);
            RpcResponse rpcResponse = RpcClientFactory.INSTANCE.sendRequestSync(rpcRequest);
            return rpcResponse.getResult();
        });
        return (T) o;
    }
}
