package easy4j.infra.rpc.client;

import easy4j.infra.rpc.integrated.IntegratedFactory;

public class RpcClientFactory {
    private static volatile RpcClient rpcClient;


    /**
     * 获取客户端
     * @return easy4j.infra.rpc.client.RpcClient
     */
    public static RpcClient getClient() {
        if (rpcClient == null) {
            synchronized (RpcClientFactory.class) {
                if (rpcClient == null) {
                    rpcClient = new RpcClient(IntegratedFactory.getConfig());
                }
            }
        }
        return rpcClient;
    }
}
