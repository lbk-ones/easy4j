package easy4j.module.datasource.dynamic;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>Spring事务执行工具类</p>
 * <p>用于在新的事务中执行回调方法并返回结果</p>
 * <p>多数据源，如果在一个加了spring注解的事务中 操作除了主数据源的其他数据源 可能会用到这个</p>
 */
@Component
public class DsExecutor {

    /**
     * 在新事务中执行回调方法，返回结果
     *
     * @param callback 回调方法接口
     * @param <T>      返回值类型
     * @return 回调方法执行结果
     * @throws Exception 如果回调方法抛出异常
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeInNewTransaction(TransactionCallback<T> callback) throws Exception {
        return callback.execute();
    }

    /**
     * 在新事务中执行回调方法，不返回结果
     *
     * @param callback 回调方法接口
     * @throws Exception 如果回调方法抛出异常
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeInNewTransaction(VoidTransactionCallback callback) throws Exception {
        callback.execute();
    }

    /**
     * 在新事务中执行回调方法（异常转换为运行时异常）
     *
     * @param callback 回调方法接口
     * @param <T>      返回值类型
     * @return 回调方法执行结果
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeInNewTransactionUnchecked(UncheckedTransactionCallback<T> callback) {
        return callback.execute();
    }

    /**
     * 有返回值的事务回调接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        /**
         * 执行业务逻辑
         *
         * @return 执行结果
         * @throws Exception 可能抛出的异常
         */
        T execute() throws Exception;
    }

    /**
     * 无返回值的事务回调接口
     */
    @FunctionalInterface
    public interface VoidTransactionCallback {
        /**
         * 执行业务逻辑
         *
         * @throws Exception 可能抛出的异常
         */
        void execute() throws Exception;
    }

    /**
     * 无受检异常的事务回调接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface UncheckedTransactionCallback<T> {
        /**
         * 执行业务逻辑
         *
         * @return 执行结果
         */
        T execute();
    }
}
