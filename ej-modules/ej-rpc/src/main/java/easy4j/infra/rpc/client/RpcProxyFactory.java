package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.RpcResponseStatus;

import java.lang.reflect.Proxy;

/**
 * 客户端 rpc 代理类
 */
public class RpcProxyFactory {

    public static <T> T getProxy(Class<T> tClass, FilterAttributes filterAttributes) {
        Object o = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, (proxy, method, args) -> {
            filterAttributes.setProxy(proxy);
            filterAttributes.setProxyMethod(method);
            filterAttributes.setProxyMethodArgs(args);
            RpcRequest rpcRequest = RpcRequest.of(method, args, filterAttributes.getServiceName());
            RpcResponse rpcResponse;
            try {
                rpcResponse = new RpcClientWrapper(RpcClientFactory.getClient())
                        .sendRequestSync(rpcRequest, filterAttributes);
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
            FilterAttributes filterAttributes = new FilterAttributes();
            filterAttributes.setGeneralizedInvoke(true);
            filterAttributes.setProxy(proxy);
            filterAttributes.setProxyMethod(method);
            filterAttributes.setProxyMethodArgs(args);
            RpcResponse rpcResponse = new RpcClientWrapper(RpcClientFactory.getClient())
                    .sendRequestSync(null, filterAttributes);
            return rpcResponse.getResult();
        });
        return ((GeneralizedInvoke) o);
    }
}
