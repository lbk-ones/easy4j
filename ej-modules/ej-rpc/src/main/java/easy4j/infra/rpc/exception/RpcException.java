package easy4j.infra.rpc.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * rpc 异常信息
 */
@Getter
public class RpcException extends RuntimeException {

    public long msgId;

    private boolean willRetry = false;

    public RpcException setMsgId(long msgId) {
        this.msgId = msgId;
        return this;
    }


    public RpcException setWillRetry(boolean willRetry) {
        this.willRetry = willRetry;
        return this;
    }

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }


    public RpcException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
