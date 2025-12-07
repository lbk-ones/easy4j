package easy4j.infra.rpc.filter.impl;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.filter.RpcFilter;
import easy4j.infra.rpc.filter.RpcFilterChain;
import easy4j.infra.rpc.filter.RpcFilterContext;

import java.util.Map;

public class RpcAttachmentsFilter implements RpcFilter {
    @Override
    public void doFilter(RpcFilterContext context, RpcFilterChain chain) {
        Map<String, Object> attachment = context.getAttachment();
        if (attachment != null && !attachment.isEmpty()) {
            RpcRequest rpcRequest = context.getRpcRequest();
            rpcRequest.setAttachment(attachment);
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
