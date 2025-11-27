package easy4j.infra.rpc.exception;

/**
 * 解码异常
 * @since 2.0.1
 * @author bokun
 */
public class DecodeRpcException extends RuntimeException{
    public DecodeRpcException() {
        super();
    }

    public DecodeRpcException(String message) {
        super(message);
    }

    public DecodeRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeRpcException(Throwable cause) {
        super(cause);
    }

    protected DecodeRpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
