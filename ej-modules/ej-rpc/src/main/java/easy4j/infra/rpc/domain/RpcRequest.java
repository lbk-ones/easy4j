package easy4j.infra.rpc.domain;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.rpc.config.CommonConstant;
import easy4j.infra.rpc.registry.ServiceControl;
import easy4j.infra.rpc.registry.jdbc.ServiceManagement;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
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

    public RpcRequest() {
        initCallerIp();
    }

    public static final class CurrentIpHolder {
        public static final String CURRENT_IP = NetUtil.getLocalhost().getHostAddress();
    }

    /**
     * 初始化调用方ip
     */
    public void initCallerIp() {
        callerIp = CurrentIpHolder.CURRENT_IP;
    }

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


    /**
     * 从request中拿取传参
     *
     * @param index  索引
     * @param tClass class类
     * @param <T>    泛型
     * @return 要拿取的对象类型
     */
    @JsonIgnore
    public <T> T takeParameterByIndex(int index, Class<T> tClass) {
        if (parameters.length > 0 && parameters.length > index) {
            Object parameter = parameters[index];
            return Convert.convert(tClass, parameter);
        }
        return null;
    }

    /**
     * 设置附件的值
     *
     * @param key   key
     * @param value value
     */
    @JsonIgnore
    public void patchAttachmentValue(String key, String value) {
        attachment.put(key, value);
    }

    /**
     * 设置附件的值
     *
     * @param key   key
     * @param value value
     */
    @JsonIgnore
    public void patchAttachmentObject(String key, Object value) {
        attachment.put(key, value);
    }

    /**
     * 设置SERVER_HANDLER的值，设置了这个值之后，不会走正常请求而是直接走handler
     *
     * @param type handler_type
     */
    @JsonIgnore
    public void serverHandler(String type) {
        patchAttachmentValue(SERVER_HANDLER, type);
    }

    /**
     * 是否是广播请求
     *
     * @param isAsync 是否异步
     */
    @JsonIgnore
    public void broadCast(boolean isAsync) {
        patchAttachmentValue(CommonConstant.IS_BROAD_CAST, "true");
        if (isAsync) {
            patchAttachmentValue(CommonConstant.IS_BROAD_CAST_ASYNC, "true");
        }
    }

    /**
     * 从附件中拿取key对应的value
     *
     * @param key 要拿取的key
     * @return 拿取的值
     */
    @JsonIgnore
    public String takeAttachmentValue(String key) {
        Object o = attachment.get(key);
        return Convert.toStr(o);
    }

    /**
     * 从附件中拿取key对应的value
     *
     * @param key 要拿取的key
     * @return 拿取的值
     */
    @JsonIgnore
    public <T> T takeAttachmentObject(String key, Class<T> aClazz) {
        Object o = attachment.get(key);
        return Convert.convert(aClazz, o);
    }

    /**
     * 判断是否是handler请求
     *
     * @return boolean
     */
    @JsonIgnore
    public boolean mayBeHandler() {
        String attachmentValue = takeAttachmentValue(SERVER_HANDLER);
        return StrUtil.isNotBlank(attachmentValue);
    }

    /**
     * 拿取SERVER_HANDLER的值
     *
     * @return String
     */
    @JsonIgnore
    public String takeServerHandler() {
        return takeAttachmentValue(SERVER_HANDLER);
    }

    /**
     * 给定一个SERVER_HANDLER看是否和当前request匹配
     *
     * @param type server_handler的值
     * @return boolean
     */
    @JsonIgnore
    public boolean matchHandler(String type) {
        return StrUtil.equals(takeAttachmentValue(SERVER_HANDLER), type) && StrUtil.isNotBlank(type);
    }

    /**
     * 将附件类型与给定值进行比对
     *
     * @param type      附件类型
     * @param typeValue 类型值
     * @return boolean
     */
    @JsonIgnore
    public boolean matchAttachment(String type, String typeValue) {
        String s = takeAttachmentValue(type);
        return StrUtil.equals(s, typeValue) && StrUtil.isNotBlank(s);
    }


    /**
     * 获取ServiceManagement
     *
     * @return boolean
     */
    @JsonIgnore
    public ServiceManagement takeServerManager() {
        return takeAttachmentObject(CommonConstant.ATTACHMENT_SERVER_MANAGER, ServiceManagement.class);
    }

    /**
     * 设置ServiceManagement
     */
    @JsonIgnore
    public void patchServerManager(ServiceManagement serviceManagement) {
        patchAttachmentObject(CommonConstant.ATTACHMENT_SERVER_MANAGER, serviceManagement);
    }


}