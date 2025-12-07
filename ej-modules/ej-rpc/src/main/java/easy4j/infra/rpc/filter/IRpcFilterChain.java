package easy4j.infra.rpc.filter;

public interface IRpcFilterChain {
    void doFilter(RpcFilterContext context);
}
