package easy4j.infra.rpc.registry;

import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.enums.RegisterType;
import easy4j.infra.rpc.integrated.IRpcConfig;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.registry.jdbc.JdbcOperate;
import easy4j.infra.rpc.registry.jdbc.JdbcRegistry;

/**
 * 获取注册中心
 *
 * @author bokun
 * @since 2.0.1
 */
public class RegistryFactory {

    private static JdbcRegistry jdbcRegistry;

    public static Registry get() {
        IRpcConfig rpcConfig = IntegratedFactory.getRpcConfig();
        E4jRpcConfig config = rpcConfig.getConfig();
        RegisterType registerType = config.getRegisterType();
        if (RegisterType.JDBC == registerType) {
            if (jdbcRegistry == null) {
                jdbcRegistry = new JdbcRegistry(new JdbcOperate());
            }
            return jdbcRegistry;
        } else {
            throw new RuntimeException("not support the type " + config.getRegisterType());
        }
    }

}
