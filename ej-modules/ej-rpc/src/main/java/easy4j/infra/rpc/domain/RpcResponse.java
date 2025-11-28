package easy4j.infra.rpc.domain;

import easy4j.infra.rpc.enums.RpcResponseStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

// RPC 响应对象
@Data
@Accessors(chain = true)
public class RpcResponse implements Serializable {
    /**
     * 对应的请求ID
     */
    private long requestId;

    /**
     * 响应状态CODE
     *
     * @see RpcResponseStatus
     */
    private int code;

    /**
     * 对应结果
     */
    private Object result;

    /**
     *
     */
    private String message;

    /**
     * 响应时间戳
     */
    private long timestamp;

    public static RpcResponse of(long requestId, RpcResponseStatus status, Object result) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(status.getCode());
        rpcResponse.setResult(result);
        rpcResponse.setRequestId(requestId);
        rpcResponse.setMessage(status.getMsg());
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse success(long requestId, Object result) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(RpcResponseStatus.SUCCESS.getCode());
        rpcResponse.setResult(result);
        rpcResponse.setRequestId(requestId);
        rpcResponse.setMessage(RpcResponseStatus.SUCCESS.getMsg());
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse error(long requestId, RpcResponseStatus status) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(status.getCode());
        rpcResponse.setResult(null);
        rpcResponse.setRequestId(requestId);
        rpcResponse.setMessage(status.getMsg());
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse error(long requestId, RpcResponseStatus status, String message) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(status.getCode());
        rpcResponse.setResult(null);
        rpcResponse.setRequestId(requestId);
        rpcResponse.setMessage(message);
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse error(long requestId, RpcResponseStatus status, Throwable throwable) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(status.getCode());
        rpcResponse.setResult(null);
        rpcResponse.setRequestId(requestId);
        if (throwable != null) {
            rpcResponse.setMessage(throwable.getMessage());
        }
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }


}