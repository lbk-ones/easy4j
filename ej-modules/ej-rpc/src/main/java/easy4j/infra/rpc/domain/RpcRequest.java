package easy4j.infra.rpc.domain;

import cn.hutool.core.util.ReflectUtil;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 调用信息封装,序列化之后客户端传给服务端
 * 服务端接受到消息
 *
 * @author bokun
 * @since 2.0.1
 */
@Data
public class RpcRequest implements Serializable {

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 唯一请求ID
     */
    private long requestId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数信息
     */
    private String[] parameterTypes;

    /**
     * 参数值
     */
    private Object[] parameters;

    /**
     * 返回类型
     */
    private String returnType;

    public RpcRequest() {
        this.requestId = atomicInteger.incrementAndGet();
    }

    /**
     * 从方法信息解析出请求对象
     * @param method method对象
     * @param args 参数信息
     * @return 请求对象
     */
    public static RpcRequest of(Method method,Object[] args){
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.requestId = atomicInteger.incrementAndGet();
        Class<?> declaringClass = method.getDeclaringClass();
        rpcRequest.methodName = method.getName();
        rpcRequest.serviceName = declaringClass.getName();
        String[] parameterTypes = new String[method.getParameterCount()];
        int i = 0;
        for (Class<?> parameterType : method.getParameterTypes()) {
            String name = parameterType.getName();
            parameterTypes[i] = name;
            i++;
        }
        rpcRequest.parameterTypes = parameterTypes;
        rpcRequest.parameters = args;
        rpcRequest.returnType = method.getReturnType().getName();
        return rpcRequest;
    }
}