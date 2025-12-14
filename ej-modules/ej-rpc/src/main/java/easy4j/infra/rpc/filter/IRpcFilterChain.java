package easy4j.infra.rpc.filter;

public interface IRpcFilterChain {
    void invoke(RpcFilterContext context);
}
