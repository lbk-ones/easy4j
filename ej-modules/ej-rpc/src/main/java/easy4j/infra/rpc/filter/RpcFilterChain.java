package easy4j.infra.rpc.filter;

import cn.hutool.core.collection.ListUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RPC 责任链管理器：调度过滤器执行
 */
public class RpcFilterChain implements IRpcFilterChain {
    // 所有过滤器（按优先级排序）
    private final List<RpcFilter> filters;

    // 构造器：初始化并排序过滤器
    public RpcFilterChain(List<RpcFilter> filters) {
        this.filters = filters.stream()
                .sorted(Comparator.comparingInt(RpcFilter::getPriority))
                .collect(Collectors.toList());
    }

    public RpcFilterChain() {
        this.filters = null;
    }

    /**
     * 执行责任链
     *
     * @param context RPC 上下文
     */
    public void doFilter(RpcFilterContext context) {
        if (this.filters == null || context.isInterrupted()) {
            return;
        }

        // 筛选符合当前端+阶段的过滤器
        Iterator<RpcFilter> iterator = filters.stream()
                .filter(filter -> matchSide(filter, context.getExecutorSide()))
                .filter(filter -> matchPhase(filter, context.getExecutorPhase()))
                .iterator();

        // 逐个执行过滤器
        while (iterator.hasNext() && !context.isInterrupted()) {
            RpcFilter filter = iterator.next();
            try {
                if (iterator.hasNext()) {
                    filter.doFilter(context, new RpcFilterChain(ListUtil.of(iterator.next())));
                } else {
                    filter.doFilter(context, new RpcFilterChain());
                }
            } catch (Throwable e) {
                // 异常隔离：单个过滤器报错不中断整个链，记录异常到上下文
                context.setException(e);
            }
        }
    }

    // 匹配执行端
    private boolean matchSide(RpcFilter filter, RpcFilterContext.ExecutorSide side) {
        RpcFilterContext.ExecutorSide executorSide = filter.getExecutorSide();
        return executorSide == RpcFilterContext.ExecutorSide.ALL
                || executorSide == side;
    }

    // 匹配执行阶段
    private boolean matchPhase(RpcFilter filter, RpcFilterContext.ExecutorPhase phase) {
        return filter.getExecutorPhase() == RpcFilterContext.ExecutorPhase.ALL
                || filter.getExecutorPhase() == phase;
    }

    // 静态构建器（简化链创建）
    public static RpcFilterChain build(List<RpcFilter> filters) {
        return new RpcFilterChain(filters);
    }
}