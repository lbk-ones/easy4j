package easy4j.infra.rpc.client;

import cn.hutool.core.util.ObjectUtil;
import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.server.ServerMethodInvoke;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 客户端 rpc 代理类
 */
@Slf4j
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
                log.error("proxy invoke exception", e);
                rpcResponse = RpcResponse.error(RpcResponse.ERROR_MSG_ID, RpcResponseStatus.INVOKE_EXCEPTION, e);
            }
            Object result = rpcResponse.getResult();
            // 这里会成为rpc的性能瓶颈 但是要改的话 没有太好的办法 等再想想，先这样吧
            if (result != null) {
                String returnType = rpcRequest.getReturnType();
                Class<?> aClass = ServerMethodInvoke.getClassByClassIdentify(returnType);
                if (void.class == aClass || ObjectUtil.isBasicType(aClass) || String.class == aClass || Object.class.getName().equals(returnType)) {
                    return result;
                }
                ISerializable jackson = SerializableFactory.getJackson();
                byte[] serializable = jackson.serializable(result);
                return jackson.deserializable(serializable, aClass);
            }
            return result;
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
