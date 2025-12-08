package easy4j.infra.rpc.filter;

import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.ExecutorSide;

/**
 * RPC 过滤器抽象接口
 * 如果要控制在服务端哈哈客户端执行那么就得重写   getExecutorSide 方法
 * 如果要控制是请求前执行还是返回后执行 重写  getExecutorPhase 方法
 *
 * @author bokun
 * @since 2.0.1
 */
public interface RpcFilter {
    /**
     * （请求前）过滤器核心处理方法
     * 抛出异常会进入 onError 回调
     *
     * @param context      RPC 上下文
     * @param chain        责任链管理器
     * @param executorSide 执行端（CLIENT/SERVER/ALL）
     */
    void invoke(RpcFilterContext context, RpcFilterChain chain, ExecutorSide executorSide);

    /**
     * （请求前出现异常）错误回调 通过这个回调可以控制异常抛出
     * @param context 上下文信息
     * @param throwable 异常信息
     */
    default void onError(RpcFilterContext context,Throwable throwable){
        context.setException(throwable);
    }

    /**
     * （响应前）结果回调，只在客户端调用服务端返回或者服务端执行完逻辑返回给客户端的时候
     * 通过这个回调可以拿到
     *
     * @param context      上下问
     * @param rpcResponse  返回结果 可能为null
     * @param executorSide 执行端
     */
    default void onResponse(RpcFilterContext context, RpcResponse rpcResponse, ExecutorSide executorSide){

    }

    /**
     * 适用的执行端（CLIENT/SERVER/ALL）
     */
    default ExecutorSide getExecutorSide() {
        return ExecutorSide.ALL;
    }
    /**
     * 过滤器优先级（数值越小，执行顺序越靠前）
     */
    default Integer getPriority() {
        return 0;
    }

    /**
     * 过滤器名称（便于日志/调试）
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}