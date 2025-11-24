package easy4j.infra.rpc.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * rpc 异常信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RpcException extends RuntimeException {

    private String code;

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, String code) {
        super(message);
        this.code = code;
    }

    public RpcException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
