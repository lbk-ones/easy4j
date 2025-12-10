package easy4j.infra.rpc.domain;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * RPC 响应对象
 * 如果rpc提供方出现异常
 */
@Data
@Accessors(chain = true)
public class RpcResponse implements Serializable {

    public static final long ERROR_MSG_ID = -1;

    /**
     * 对应的请求ID
     */
    private long msgId;

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

    /**
     * 响应时间戳
     */
    private long cost;

    /**
     * 调用的时候出现未知异常，不会被序列化到客户端
     */
    @JsonIgnore
    private transient Throwable unknownException;

    public static RpcResponse of(long msgId, RpcResponseStatus status, Object result) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(status.getCode());
        rpcResponse.setResult(result);
        rpcResponse.setMsgId(msgId);
        rpcResponse.setMessage(status.getMsg());
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse success(long msgId) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(RpcResponseStatus.SUCCESS.getCode());
        rpcResponse.setResult(null);
        rpcResponse.setMsgId(msgId);
        rpcResponse.setMessage(RpcResponseStatus.SUCCESS.getMsg());
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse success(long msgId, Object result) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(RpcResponseStatus.SUCCESS.getCode());
        rpcResponse.setResult(result);
        rpcResponse.setMsgId(msgId);
        rpcResponse.setMessage(RpcResponseStatus.SUCCESS.getMsg());
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse error(long msgId, RpcResponseStatus status) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(status.getCode());
        rpcResponse.setResult(null);
        rpcResponse.setMsgId(msgId);
        rpcResponse.setMessage(status.getMsg());
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse error(long msgId, RpcResponseStatus status, String message) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(status.getCode());
        rpcResponse.setResult(null);
        rpcResponse.setMsgId(msgId);
        if (StrUtil.isNotBlank(message)) {
            rpcResponse.setMessage(message);
        } else {
            rpcResponse.setMessage(status.getMsg());
        }
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }

    public static RpcResponse error(long msgId, RpcResponseStatus status, Throwable throwable) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(status.getCode());
        rpcResponse.setResult(null);
        rpcResponse.setMsgId(msgId);
        if (throwable != null) {
            rpcResponse.setMessage(throwable.getMessage());
        }
        rpcResponse.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return rpcResponse;
    }


}