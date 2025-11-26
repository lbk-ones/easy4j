package easy4j.infra.rpc.client;

import easy4j.infra.rpc.config.ClientConfig;
import easy4j.infra.rpc.utils.Host;

public class RpcClientFactory {
    public static RpcClient INSTANCE = new RpcClient(new ClientConfig());
    public static RpcClient of(Host host) {
        ClientConfig DEFAULT_CLIENT_CONFIG = new ClientConfig();
        DEFAULT_CLIENT_CONFIG.setHost(host.getIp());
        DEFAULT_CLIENT_CONFIG.setPort(host.getPort());
        return new RpcClient(DEFAULT_CLIENT_CONFIG);
    }
}
