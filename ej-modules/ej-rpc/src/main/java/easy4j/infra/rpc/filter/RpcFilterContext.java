package easy4j.infra.rpc.filter;

import easy4j.infra.rpc.domain.ProxyAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;

import java.util.Map;

/**
 * RPC 过滤器上下文（封装全量上下文信息）
 * <p>
 * 客户端：
 * 拦截开始
 * 拦截返回
 * <p>
 * 服务端：
 * 拦截开始
 * 拦截返回
 */
public interface RpcFilterContext {


    /**
     * 获取请求对象（可修改）
     */
    RpcRequest getRpcRequest();

    /**
     * 响应结果（可修改）
     * 客户端：有了就代表客户端已经接受到服务端的信息，
     * 服务端：有了就代表服务端已经执行完逻辑并有了结果
     */
    RpcResponse getRpcResponse();


    void setRpcRequest(RpcRequest rpcRequest);

    void setRpcResponse(RpcResponse rpcResponse);

    /**
     * 附件（透传信息，如 Token、TraceID）
     *
     * @param key
     */
    String getAttachment(String key);

    Map<String, Object> getAttachment();

    Object getObjectAttachment(String key);

    void setObjectAttachment(String key, Object object);

    void setAttachment(String key, String value);

    // 异常信息
    Throwable getException();

    void setException(Throwable e);


    ExecutorSide getExecutorSide();

    void setExecutorSide(ExecutorSide executorSide);

    /**
     * 返回执行阶段，客户端，服务端
     */
    ExecutorPhase getExecutorPhase();

    void setExecutorPhase(ExecutorPhase executorPhase);

    String getCallerIp();

    void setCallerIp(String callerIp);


    /**
     * 是否被打断
     */
    boolean isInterrupted();

    /**
     * 打断
     */
    void interrupted();

    ProxyAttributes getProxyAttributes();

    void setProxyAttributes(ProxyAttributes proxyAttributes);


    // 枚举：执行端
    enum ExecutorSide {
        ALL, CLIENT, SERVER
    }

    // 枚举：执行阶段
    enum ExecutorPhase {
        ALL,
        // 客户端发送请求前 / 服务端接收请求前
        REQUEST_BEFORE,
        // 服务端返回响应前 / 客户端接收响应后
        RESPONSE_BEFORE
    }
}