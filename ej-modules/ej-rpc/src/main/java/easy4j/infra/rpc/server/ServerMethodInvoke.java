package easy4j.infra.rpc.server;

import cn.hutool.core.exceptions.InvocationTargetRuntimeException;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.integrated.ServerInstanceInit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 服务端方法调用
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
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
            // 下面这几个异常可以不用setUnknownException反正都是直接发送会客户端
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return RpcResponse.error(msgId, RpcResponseStatus.RESOURCE_NOT_FOUND);
        } catch (InvocationTargetRuntimeException exception) {
            return RpcResponse.error(msgId, RpcResponseStatus.INVOKE_EXCEPTION, exception.getCause());
        } catch (Throwable ex) {
            log.error("invoke error", ex);
            return RpcResponse.error(msgId, RpcResponseStatus.INVOKE_EXCEPTION, ex).setUnknownException(ex);
        }
    }

    public static Class<?> getClassByClassIdentify(String classIdentify) {
        if (StrUtil.isBlank(classIdentify)) return null;
        Class<?> re = switch (classIdentify) {
            case "int" -> int.class;
            case "byte" -> byte.class;
            case "short" -> short.class;
            case "double" -> double.class;
            case "float" -> float.class;
            case "boolean" -> boolean.class;
            case "long" -> long.class;
            case "char" -> char.class;
            case "void" -> void.class;
            default -> null;
        };
        if (re != null) return re;
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
        String classIdentify = request.getInterfaceIdentify();
        aClass = getClassByClassIdentify(classIdentify);
        if (aClass == null) {
            throw new ClassNotFoundException();
        }
        String methodName = request.getMethodName();
        parameterTypes = Arrays.stream(request.getParameterTypes()).map(ServerMethodInvoke::getClassByClassIdentify).toList().toArray(new Class[]{});
        if (Arrays.stream(parameterTypes).anyMatch(Objects::isNull)) {
            throw new RpcException("There is an unknown type in the parameterTypes parameter");
        }
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
        ServerInstanceInit serverInstanceInit = IntegratedFactory.getServerInstanceInit();
        return serverInstanceInit.instance(request);
    }


}
