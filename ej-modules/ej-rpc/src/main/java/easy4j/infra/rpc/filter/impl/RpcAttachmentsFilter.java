package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.config.CommonConstant;
import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.enums.ExecutorSide;
import easy4j.infra.rpc.filter.Activate;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

import java.util.Map;

/**
 * 偏后执行，主要执行附件逻辑，从上下文拿取附件赋值到请求体当中
 * ps:上下文的附件会覆盖请求体中原来的附件
 */
@Activate(group = ExecutorSide.CLIENT)
public class RpcAttachmentsFilter implements RpcFilter {
    @Override
    public void invoke(RpcFilterContext context, RpcFilterChain chain, ExecutorSide executorSide) {
        FilterAttributes filterAttributes = context.getFilterAttributes();
        if (filterAttributes != null) {
            if (filterAttributes.isGeneralizedInvoke()) {
                context.setAttachment(CommonConstant.IS_GENERALIZED_INVOKE, "true");
            }
        }
        Map<String, Object> attachment = context.getAttachment();
        if (attachment != null && !attachment.isEmpty()) {
            RpcRequest rpcRequest = context.getRpcRequest();
            Map<String, Object> attachment1 = rpcRequest.getAttachment();
            if (!attachment1.isEmpty()) {
                attachment1.putAll(attachment);
            }
            rpcRequest.setAttachment(attachment);
        }
        chain.invoke(context);
    }

    @Override
    public Integer getPriority() {
        return Integer.MAX_VALUE;
    }
}
