package easy4j.infra.rpc.filter;

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
     * 过滤器核心处理方法
     *
     * @param context RPC 上下文
     * @param chain   责任链管理器
     */
    void doFilter(RpcFilterContext context, RpcFilterChain chain);

    /**
     * 过滤器优先级（数值越小，执行顺序越靠前）
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 适用的执行端（CLIENT/SERVER/ALL）
     */
    default RpcFilterContext.ExecutorSide getExecutorSide() {
        return RpcFilterContext.ExecutorSide.ALL;
    }

    /**
     * 适用的执行阶段（REQUEST_BEFORE/RESPONSE_BEFORE/ALL）
     */
    default RpcFilterContext.ExecutorPhase getExecutorPhase() {
        return RpcFilterContext.ExecutorPhase.ALL;
    }

    /**
     * 过滤器名称（便于日志/调试）
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}