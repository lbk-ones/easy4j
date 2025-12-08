package easy4j.infra.rpc.server;

import cn.hutool.core.exceptions.InvocationTargetRuntimeException;
import cn.hutool.core.util.ReflectUtil;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.integrated.ServerInstanceInit;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 服务端方法调用
 *
 * @author bokun
 * @since 2.0.1
 */
public class ServerMethodInvoke {
    private static final Map<String, Class<?>> cacheClass = new ConcurrentHashMap<>();
    private static final Map<String, Method> methodMap = new ConcurrentHashMap<>();
    private Class<?> aClass;
    private Method method;
    private Object[] parameters;
    private Class<?>[] parameterTypes;
    @Getter
    private final RpcRequest request;

    @Getter
    private final Transport transport;

    public ServerMethodInvoke(RpcRequest request, Transport transport_) {
        this.request = request;
        this.transport = transport_;
    }


    public RpcResponse invoke() {
        long msgId = transport.getMsgId();
        try {
            // init
            init(request);
            Object instance = getClassInstance();
            if (null == instance) return RpcResponse.error(msgId, RpcResponseStatus.INSTANCE_NOT_FOUND);
            Object invoke = ReflectUtil.invoke(instance, method, parameters);
            return RpcResponse.success(msgId, invoke);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return RpcResponse.error(msgId, RpcResponseStatus.RESOURCE_NOT_FOUND);
        } catch (InvocationTargetRuntimeException exception) {
            return RpcResponse.error(msgId, RpcResponseStatus.INVOKE_EXCEPTION, exception.getCause());
        } catch (Exception ex) {
            return RpcResponse.error(msgId, RpcResponseStatus.INVOKE_EXCEPTION, ex);
        }
    }

    public static Class<?> getClassByClassIdentify(String classIdentify) {
        return cacheClass.computeIfAbsent(classIdentify, e ->
                {
                    try {
                        return Class.forName(e);
                    } catch (ClassNotFoundException ex) {
                        return null;
                    }
                }
        );
    }

    private void init(RpcRequest request) throws ClassNotFoundException, NoSuchMethodException {
        String classIdentify = request.getClassIdentify();
        aClass = getClassByClassIdentify(classIdentify);
        if (aClass == null) {
            throw new ClassNotFoundException();
        }
        String methodName = request.getMethodName();
        parameterTypes = Arrays.stream(request.getParameterTypes()).map(e -> {
            try {
                return aClass.getClassLoader().loadClass(e);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }).toList().toArray(new Class[]{});
        parameters = request.getParameters();
        method = methodMap.computeIfAbsent(classIdentify + "#" + methodName, e -> {
            try {
                return aClass.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        });
        if (method == null) {
            throw new NoSuchMethodException();
        }
    }

    private Object getClassInstance() {
        ServerInstanceInit serverInstanceInit = IntegratedFactory.getOrDefault(ServerInstanceInit.class, () -> new DefaultServerInstanceInit(aClass));
        return serverInstanceInit.instance(request);
    }

    static class DefaultServerInstanceInit implements ServerInstanceInit {

        Class<?> aClass;

        public DefaultServerInstanceInit(Class<?> aClass) {
            this.aClass = aClass;
        }

        @Override
        public Object instance(RpcRequest request) {
            return ReflectUtil.newInstanceIfPossible(aClass);
        }
    }
}
