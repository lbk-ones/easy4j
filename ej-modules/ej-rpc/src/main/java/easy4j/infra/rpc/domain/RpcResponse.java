package easy4j.infra.rpc.domain;

import lombok.Data;

import java.io.Serializable;

// RPC 响应对象
@Data
public class RpcResponse implements Serializable {
    private long requestId; // 对应请求的ID
    private int status; // 响应状态（0=成功，1=失败）
    private Object result; // 业务结果
    private String errorMsg; // 错误信息（失败时）
}