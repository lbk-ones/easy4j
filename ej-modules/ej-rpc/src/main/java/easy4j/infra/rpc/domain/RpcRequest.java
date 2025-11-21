package easy4j.infra.rpc.domain;

import java.io.Serializable;

public class RpcRequest implements Serializable {
    private String requestId; // 请求ID（唯一标识）
    private String serviceName; // 目标服务名（如 "UserService"）
    private String methodName; // 目标方法名（如 "getUser"）
    private Class<?>[] parameterTypes; // 参数类型
    private Object[] parameters; // 参数值

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}