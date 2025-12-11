package easy4j.infra.rpc.retry;

/**
 * 业务处理接口
 *
 * @param <T> 元素类型
 */
@FunctionalInterface
public interface ConsumerHandler<T> {
    void handle(T data) throws Exception;
}