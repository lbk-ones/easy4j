package easy4j.infra.rpc.domain;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 调用信息封装,序列化之后客户端传给服务端
 * 服务端接受到消息
 *
 * @author bokun
 * @since 2.0.1
 */
@Data
public class RpcRequest implements Serializable {

    public static final String SERVER_HANDLER = "$SERVER_HANDLER";


    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 接口类的标识
     */
    private String interfaceIdentify;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数信息
     */
    private String[] parameterTypes = new String[0];

    /**
     * 参数值
     */
    private Object[] parameters = new Object[0];

    /**
     * 返回类型
     */
    private String returnType;

    /**
     * 调用方IP
     */
    private String callerIp;

    /**
     * 附加参数信息 会一块传递到服务端去
     */
    private Map<String, Object> attachment = new ConcurrentHashMap<>();


    /**
     * 从方法信息解析出请求对象
     *
     * @param method      method对象
     * @param args        参数信息
     * @param serviceName 服务名称
     * @return 请求对象
     */
    public static RpcRequest of(Method method, Object[] args, String serviceName) {
        RpcRequest rpcRequest = new RpcRequest();
        Class<?> declaringClass = method.getDeclaringClass();
        rpcRequest.methodName = method.getName();
        rpcRequest.serviceName = serviceName;
        // 但是这个一定是类的全类名
        rpcRequest.interfaceIdentify = declaringClass.getName();
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


    public <T> T getParametersIndex(int index, Class<T> tClass) {
        if (parameters.length > 0 && parameters.length > index) {
            Object parameter = parameters[index];
            return Convert.convert(tClass, parameter);
        }
        return null;
    }

    public void setAttachmentValue(String key, String value) {
        attachment.put(key, value);
    }

    public String getAttachmentValue(String key) {
        Object o = attachment.get(key);
        return Convert.toStr(o);
    }

    public void serverHandler(String type) {
        setAttachmentValue(SERVER_HANDLER, type);
    }

    public boolean mayBeHandler() {
        String attachmentValue = getAttachmentValue(SERVER_HANDLER);
        return StrUtil.isNotBlank(attachmentValue);
    }

    public String getServerHandler() {
        return getAttachmentValue(SERVER_HANDLER);
    }

    public boolean matchHandler(String type) {
        return StrUtil.equals(getAttachmentValue(SERVER_HANDLER), type) && StrUtil.isNotBlank(type);
    }


}