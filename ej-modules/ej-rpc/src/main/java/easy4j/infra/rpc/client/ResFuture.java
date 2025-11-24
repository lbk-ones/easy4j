package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.RpcResponse;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 等待结果，处理超时
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
@Data
public class ResFuture {
    private static final ConcurrentHashMap<Long, ResFuture> FUTURE_TABLE = new ConcurrentHashMap<>();

    private final long requestId;

    // remove the timeout
    private final long timeoutMillis;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final long beginTimestamp = System.currentTimeMillis();

    private RpcResponse rpcResponse;

    private volatile boolean sendOk = true;

    private Throwable cause;

    public ResFuture(long requestId, long timeoutMillis) {
        this.requestId = requestId;
        this.timeoutMillis = timeoutMillis;
        FUTURE_TABLE.put(requestId, this);
    }

    /**
     * wait for response
     *
     * @return command
     */
    public RpcResponse waitResponse() throws InterruptedException {
        if (!latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            log.warn("Wait response in {}/ms timeout, request id {}", timeoutMillis, requestId);
        }
        return this.rpcResponse;
    }

    public void putResponse(final RpcResponse iRpcResponse) {
        this.rpcResponse = iRpcResponse;
        this.latch.countDown();
        FUTURE_TABLE.remove(requestId);
    }

    public static ResFuture getFuture(long opaque) {
        return FUTURE_TABLE.get(opaque);
    }

    /**
     * whether timeout
     *
     * @return timeout
     */
    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

}
