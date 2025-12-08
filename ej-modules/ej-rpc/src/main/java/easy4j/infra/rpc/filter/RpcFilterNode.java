package easy4j.infra.rpc.filter;

import lombok.Data;

@Data
public class RpcFilterNode {
    private RpcFilter current;
    private RpcFilterNode next;

    public static RpcFilterNode build(RpcFilter rpcFilter) {
        RpcFilterNode rpcFilterNode = new RpcFilterNode();
        rpcFilterNode.setCurrent(rpcFilter);
        return rpcFilterNode;
    }

}