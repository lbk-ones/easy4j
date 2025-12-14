package easy4j.infra.rpc.filter;

import cn.hutool.core.util.ReflectUtil;
import easy4j.infra.rpc.enums.ExecutorSide;
import easy4j.infra.rpc.filter.impl.TailContextFilter;
import easy4j.infra.rpc.utils.SpiUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bokun
 * @since 2.0.1
 */
public abstract class AbstractRpcWrapper {

    /**
     * 获取头节点
     *
     * @return 头节点
     */
    public RpcFilterNode getFirstFilterNode(RpcFilterContext rpcFilterContext) {
        List<RpcFilter> load_ = SpiUtils.load(RpcFilter.class);
        load_.add(new TailContextFilter());
        ExecutorSide executorSide = rpcFilterContext.getExecutorSide();
        List<RpcFilter> load = getRpcFilters(rpcFilterContext, load_, executorSide);
        List<RpcFilterNode> filterNodes = new ArrayList<>();
        for (RpcFilter rpcFilter : load) {
            RpcFilterNode build = RpcFilterNode.build(rpcFilter);
            filterNodes.add(build);
        }
        RpcFilterNode rpcFilterNode = filterNodes.get(0);
        final RpcFilterNode firstNode = rpcFilterNode;
        int size = filterNodes.size();
        if (size > 1) {
            List<RpcFilterNode> filterNodes1 = filterNodes.subList(1, size);
            for (RpcFilterNode next : filterNodes1) {
                rpcFilterNode.setNext(next);
                rpcFilterNode = next;
            }
        }
        return firstNode;
    }

    private static List<RpcFilter> getRpcFilters(RpcFilterContext rpcFilterContext, List<RpcFilter> load_, ExecutorSide executorSide) {
        return load_.stream().filter(rpcFilter -> {
            boolean flag = false;
            Class<? extends RpcFilter> aClass = rpcFilter.getClass();
            if (aClass.isAnnotationPresent(Activate.class)) {
                Activate annotation = aClass.getAnnotation(Activate.class);
                ExecutorSide[] group = annotation.group();
                boolean sideMatch = Arrays.stream(group).anyMatch(e -> e == executorSide || e == ExecutorSide.ALL);
                String[] strings = annotation.hasValue();
                Map<String, Object> attachment = rpcFilterContext.getAttachment();
                boolean valueMatch = Arrays.stream(strings).allMatch(e -> {
                    Object fieldValue = ReflectUtil.getFieldValue(rpcFilterContext, e);
                    return fieldValue != null || attachment.get(e) != null;
                });
                if (group.length > 0 && strings.length > 0) {
                    if (sideMatch && valueMatch) flag = true;
                } else {
                    if (sideMatch || valueMatch) flag = true;
                }
            } else {
                ExecutorSide executorSide2 = rpcFilter.getExecutorSide();
                if (executorSide2 == executorSide || executorSide2 == ExecutorSide.ALL) flag = true;
            }
            return flag;
        }).sorted(Comparator.comparing(RpcFilter::getPriority)).collect(Collectors.toList());
    }
}
