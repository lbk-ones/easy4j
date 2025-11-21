package easy4j.infra.rpc.client;

import easy4j.infra.rpc.config.ClientConfig;
import easy4j.infra.rpc.config.ServerConfig;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxyFactory {
    ClientConfig config;

    public RpcProxyFactory(ClientConfig config) {
        this.config = config;
    }

    public <T> T getProxy(Class<T> tClass){
        Object o = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                RpcClient rpcClient = new RpcClient(null,0);
                RpcRequest rpcRequest = new RpcRequest();
                RpcResponse rpcResponse = rpcClient.sendRequest(rpcRequest);
                return rpcResponse.getResult();
            }
        });
        return (T) o;
    }
}
