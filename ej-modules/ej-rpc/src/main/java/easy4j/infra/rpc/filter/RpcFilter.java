package easy4j.infra.rpc.filter;

import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.ExecutorSide;

/**
 * RPC 过滤器抽象接口
 * 如果要控制在服务端哈哈客户端执行那么就得重写 getExecutorSide方法或者使用 @Activate注解
 *
 * @author bokun
 * @since 2.0.1
 */
public interface RpcFilter {
    /**
     * （请求前）
     * 客户端和服务端invoke这个方法都不会接受抛出的异常 默认行为只会保存异常
     * 如果抛出异常则会进入 onError 回调一次，如果onError不处理，这个异常可能就会被后面的异常覆盖
     * ps: 如果想要在 invoke抛出异常直接抛到调用处，那么就在onError回调中直接 throw
     *
     * @param context      RPC 上下文
     * @param chain        责任链管理器
     * @param executorSide 执行端（CLIENT/SERVER/ALL）
     */
    void invoke(RpcFilterContext context, RpcFilterChain chain, ExecutorSide executorSide);

    /**
     * （请求前出现异常）错误回调 通过这个回调可以控制异常抛出
     * ps: 如果是服务端，那么在这里直接抛出异常会直接发送会客户端的
     *
     * @param context   上下文信息
     * @param throwable 异常信息
     */
    default void onError(RpcFilterContext context, Throwable throwable) {
        context.setException(throwable);
    }

    /**
     * （响应前）
     * 结果回调，只在客户端调用服务端返回或者服务端执行完逻辑返回给客户端的时候
     * 调用前和调用时的异常会被汇总到 RpcResponse#unknownException
     * 通过这个回调可以拿到返回结果RpcResponse通过RpcResponse可以判断调用过程有没有出现异常
     * ps: 如果是服务端在onResponse方法抛出异常那么异常的message会包装成RpcResponse直接返回客户端
     *
     * @param context      上下问
     * @param rpcResponse  返回结果 可能为null
     * @param executorSide 执行端
     */
    default void onResponse(RpcFilterContext context, RpcResponse rpcResponse, ExecutorSide executorSide) {

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