package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.RpcResponseStatus;

import java.lang.reflect.Proxy;

/**
 * 客户端 rpc 代理类
 */
public class RpcProxyFactory {

    public static <T> T getProxy(Class<T> tClass, String serviceName) {
        Object o = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, (proxy, method, args) -> {
            RpcRequest rpcRequest = RpcRequest.of(method, args, serviceName);
            RpcResponse rpcResponse = RpcClientFactory.getClient().sendRequestSync(rpcRequest);
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
            if (args.length == 0) {
                return RpcResponse.error(0, RpcResponseStatus.PARAM_MISSING);
            }
            Object arg = args[0];
            if(arg == null){
                return RpcResponse.error(0, RpcResponseStatus.PARAM_MISSING);
            }
            RpcRequest arg1 = (RpcRequest) arg;
            RpcResponse rpcResponse = RpcClientFactory.getClient().sendRequestSync(arg1);
            return rpcResponse.getResult();
        });
        return ((GeneralizedInvoke) o);
    }
}
