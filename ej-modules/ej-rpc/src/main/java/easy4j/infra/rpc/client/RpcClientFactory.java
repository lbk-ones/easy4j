package easy4j.infra.rpc.client;

import easy4j.infra.rpc.config.ClientConfig;

public class RpcClientFactory {

    public static final ClientConfig DEFAULT_CLIENT_CONFIG = ClientConfig.builder().build();

    public static RpcClient INSTANCE = newClient(DEFAULT_CLIENT_CONFIG);

    public static RpcClient newClient(ClientConfig config) {
        return new RpcClient(config);
    }
}
