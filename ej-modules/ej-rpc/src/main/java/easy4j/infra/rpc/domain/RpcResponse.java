package easy4j.infra.rpc.domain;

import easy4j.infra.rpc.enums.RpcResponseStatus;
import lombok.Data;

import java.io.Serializable;

// RPC 响应对象
@Data
public class RpcResponse implements Serializable {
    /**
     * 对应的请求ID
     */
    private long requestId;
    /**
     * 响应状态CODE
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


}