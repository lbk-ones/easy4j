package easy4j.infra.rpc.domain;

import java.io.Serializable;

// RPC 响应对象
public class RpcResponse implements Serializable {
    private String requestId; // 对应请求的ID
    private int status; // 响应状态（0=成功，1=失败）
    private Object result; // 业务结果
    private String errorMsg; // 错误信息（失败时）

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}