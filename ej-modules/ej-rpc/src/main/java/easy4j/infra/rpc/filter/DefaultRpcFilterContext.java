package easy4j.infra.rpc.filter;

import cn.hutool.core.convert.Convert;
import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.ExecutorPhase;
import easy4j.infra.rpc.enums.ExecutorSide;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 调用上下文默认实现
 *
 * @author bokun
 * @since 2.0.1
 */
public class DefaultRpcFilterContext implements RpcFilterContext {

    boolean interrupted = false;
    RpcRequest rpcRequest;
    RpcResponse rpcResponse;
    Map<String, Object> attachment = new ConcurrentHashMap<>();
    Throwable exception;
    ExecutorSide executorSide;
    ExecutorPhase executorPhase;

    @Setter
    @Getter
    FilterAttributes filterAttributes;

    String callerIp;

    @Override
    public RpcRequest getRpcRequest() {
        return rpcRequest;
    }

    @Override
    public RpcResponse getRpcResponse() {
        return rpcResponse;
    }

    @Override
    public void setRpcRequest(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }

    @Override
    public void setRpcResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
    }

    @Override
    public String getAttachment(String key) {
        return Convert.toStr(attachment.get(key));
    }

    @Override
    public Map<String, Object> getAttachment() {
        return attachment;
    }

    @Override
    public void setAttachment(String key, String value) {
        attachment.put(key, value);
    }

    @Override
    public Object getObjectAttachment(String key) {
        return attachment.get(key);
    }

    @Override
    public void setObjectAttachment(String key, Object object) {
        attachment.put(key, object);
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public void setException(Throwable e) {
        exception = e;
    }

    @Override
    public ExecutorSide getExecutorSide() {
        return executorSide;
    }

    @Override
    public void setExecutorSide(ExecutorSide executorSide) {
        this.executorSide = executorSide;
    }

    @Override
    public ExecutorPhase getExecutorPhase() {
        return this.executorPhase;
    }

    @Override
    public void setExecutorPhase(ExecutorPhase executorPhase) {
        this.executorPhase = executorPhase;
    }

    @Override
    public String getCallerIp() {
        return callerIp;
    }

    @Override
    public void setCallerIp(String callerIp) {
        this.callerIp = callerIp;
    }

    @Override
    public boolean isInterrupted() {
        return interrupted;
    }

    @Override
    public void interrupted() {
        interrupted = true;
    }

}
