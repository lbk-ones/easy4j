package easy4j.infra.rpc.client;

import easy4j.infra.rpc.integrated.IntegratedFactory;

public class RpcClientFactory {

    public static RpcClient getClient() {
        return new RpcClient(IntegratedFactory.getRpcConfig().getConfig());
    }
}
