package easy4j.infra.rpc.client;

import easy4j.infra.rpc.config.ClientConfig;

public class RpcClientFactory {

    public static RpcClient INSTANCE = newClient(ClientConfig.builder().build());

    public static RpcClient newClient(ClientConfig config){
        return new RpcClient(config);
    }
}
