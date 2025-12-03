package easy4j.infra.rpc.server;

import easy4j.infra.rpc.integrated.IntegratedFactory;

public class RpcServerFactory {

    public static RpcServer getRpcServer() {
        return new RpcServer(IntegratedFactory.getRpcConfig().getConfig());
    }

}
