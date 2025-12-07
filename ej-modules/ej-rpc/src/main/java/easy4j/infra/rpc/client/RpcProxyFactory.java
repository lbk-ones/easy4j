package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.ProxyAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.RpcResponseStatus;

import java.lang.reflect.Proxy;

/**
 * 客户端 rpc 代理类
 */
public class RpcProxyFactory {

    public static <T> T getProxy(Class<T> tClass, ProxyAttributes proxyAttributes) {
        Object o = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, (proxy, method, args) -> {
            proxyAttributes.setProxy(proxy);
            proxyAttributes.setProxyMethod(method);
            proxyAttributes.setProxyMethodArgs(args);
            RpcRequest rpcRequest = RpcRequest.of(method, args, proxyAttributes.getServiceName());
            RpcResponse rpcResponse = null;
            try {
                rpcResponse = new RpcClientWrapper(RpcClientFactory.getClient())
                        .sendRequestSync(rpcRequest, proxyAttributes);
            } catch (Exception e) {
                rpcResponse = RpcResponse.error(RpcResponse.ERROR_MSG_ID, RpcResponseStatus.INVOKE_EXCEPTION, e);
            }
            return rpcResponse.getResult();
        });
        return (T) o;
    }


    /**
     * 泛化调用
     *
     * @return easy4j.infra.rpc.client.GeneralizedInvoke
     */
    public static GeneralizedInvoke getGeneralizedProxy() {
        Object o = Proxy.newProxyInstance(GeneralizedInvoke.class.getClassLoader(), new Class[]{GeneralizedInvoke.class}, (proxy, method, args) -> {
            ProxyAttributes proxyAttributes = new ProxyAttributes();
            proxyAttributes.setGeneralizedInvoke(true);
            proxyAttributes.setProxy(proxy);
            proxyAttributes.setProxyMethod(method);
            proxyAttributes.setProxyMethodArgs(args);
            RpcResponse rpcResponse = new RpcClientWrapper(RpcClientFactory.getClient())
                    .sendRequestSync(null, proxyAttributes);
            return rpcResponse.getResult();
        });
        return ((GeneralizedInvoke) o);
    }
}
