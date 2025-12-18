package easy4j.infra.rpc.client;

import cn.hutool.core.util.ObjectUtil;
import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.serializable.*;
import easy4j.infra.rpc.serializable.kryo.KryoSerializable;
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
            Object method1 = checkObject(proxy, method, args);
            if (method1 != null) return method1;
            filterAttributes.setProxy(proxy);
            filterAttributes.setProxyMethod(method);
            filterAttributes.setProxyMethodArgs(args);
            RpcRequest rpcRequest = RpcRequest.of(method, args, filterAttributes.getServiceName());
            RpcResponse rpcResponse = new RpcClientWrapper(RpcClientFactory.getClient())
                    .sendRequestSync(rpcRequest, filterAttributes);
            Object result = rpcResponse.getResult();
            String returnType = rpcRequest.getReturnType();
            return deserializableObj(returnType,result);
        });
        return (T) o;
    }

    /**
     * 根据协议来反序列化
     * @param returnType 返回对象类型
     * @param result 返回对象
     * @return 对应的类型
     */
    private static Object deserializableObj(String returnType,Object result){
        if(result == null) return null;
        ISerializable iSerializable = SerializableFactory.get();
        if(iSerializable instanceof HessionSerializable || iSerializable instanceof KryoSerializable){
            return result;
        }else if(iSerializable instanceof JacksonSerializable){
            // 如果是使用jackson这里要再次反序列化一次，因为默认jackson对嵌套对象会反序列化成LinkedHashMap类型对不上
            Class<?> aClass = ServerMethodInvoke.getClassByClassIdentify(returnType);
            if (aClass != null) {
                if (void.class == aClass || ObjectUtil.isBasicType(aClass) || String.class == aClass || Object.class.getName().equals(returnType)) {
                    return result;
                }
                ISerializable jackson = SerializableFactory.getJackson();
                byte[] serializable = jackson.serializable(result);
                return jackson.deserializable(serializable, aClass);
            }
            return result;
        }

        return result;
    }

    /**
     * 泛化调用
     *
     * @return easy4j.infra.rpc.client.GeneralizedInvoke
     */
    public static GeneralizedInvoke getGeneralizedProxy() {
        Object o = Proxy.newProxyInstance(GeneralizedInvoke.class.getClassLoader(), new Class[]{GeneralizedInvoke.class}, (proxy, method, args) -> {
            Object method1 = checkObject(proxy, method, args);
            if (method1 != null) return method1;
            FilterAttributes filterAttributes = new FilterAttributes();
            filterAttributes.setGeneralizedInvoke(true);
            filterAttributes.setProxy(proxy);
            filterAttributes.setProxyMethod(method);
            filterAttributes.setProxyMethodArgs(args);
            RpcResponse rpcResponse = new RpcClientWrapper(RpcClientFactory.getClient())
                    .sendRequestSync(null, filterAttributes);
            Object result = rpcResponse.getResult();
            String returnType = ((RpcRequest)args[0]).getReturnType();
            return deserializableObj(returnType,result);
        });
        return ((GeneralizedInvoke) o);
    }

    private static Object checkObject(Object proxy, Method method, Object[] args) {
        // 过滤Object类的通用方法，执行默认逻辑
        if (method.getDeclaringClass() == Object.class) {
            switch (method.getName()) {
                case "toString":
                    // 自定义toString，避免触发RPC
                    return "RPCProxy[" + method.getDeclaringClass().getName() + "]";
                case "hashCode":
                    return System.identityHashCode(proxy);

                case "equals":
                    return proxy == args[0];
            }
        }
        return null;
    }
}
