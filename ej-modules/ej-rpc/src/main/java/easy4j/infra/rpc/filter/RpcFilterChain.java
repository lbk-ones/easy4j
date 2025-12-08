package easy4j.infra.rpc.filter;

import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.enums.ExecutorPhase;

/**
 * RPC 责任链管理器：调度过滤器执行
 */
public final class RpcFilterChain implements IRpcFilterChain {
    // 所有过滤器（按优先级排序）
    private RpcFilterNode filters;

    // 构造器：初始化并排序过滤器
    public RpcFilterChain(RpcFilterNode nodeFilter) {
        this.filters = nodeFilter;
    }

    /**
     * 节点重置
     *
     * @param node 第一个节点
     */
    public void reset(RpcFilterNode node) {
        this.filters = node;
    }

    /**
     * 执行责任链
     *
     * @param context RPC 上下文
     */
    public void invoke(RpcFilterContext context) {
        if (this.filters == null || context.isInterrupted()) {
            return;
        }
        RpcFilter current = this.filters.getCurrent();
        ExecutorPhase executorPhase = context.getExecutorPhase();
        if (ExecutorPhase.REQUEST_BEFORE == executorPhase) {
            // one by one exe filter
            try {
                current.invoke(context, new RpcFilterChain(this.filters.getNext()), context.getExecutorSide());
            } catch (Throwable e) {
                // error handler callback default been set context
                current.onError(context, e);
            }
        } else if (ExecutorPhase.RESPONSE_BEFORE == executorPhase) {
            // response callback not handler exception
            do {
                RpcResponse rpcResponse = context.getRpcResponse();
                this.filters.getCurrent().onResponse(context, rpcResponse, context.getExecutorSide());
                this.filters = this.filters.getNext();
            } while (this.filters != null);
        }
    }

    /**
     * 静态构建器（简化链创建）
     *
     * @param filters 过滤节点
     * @return RpcFilterChain
     */
    public static RpcFilterChain build(RpcFilterNode filters) {
        return new RpcFilterChain(filters);
    }
}