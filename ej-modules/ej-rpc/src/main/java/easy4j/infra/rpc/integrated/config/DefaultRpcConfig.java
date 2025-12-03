package easy4j.infra.rpc.integrated.config;


import easy4j.infra.rpc.config.E4jRpcConfig;

public class DefaultRpcConfig extends AbstractRpcConfig {

    @Override
    public String defaultGet(String key) {
        return null;
    }

    @Override
    public E4jRpcConfig getConfig() {
        return new E4jRpcConfig();
    }
}
