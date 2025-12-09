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

    private static final class JdbcRegistryHolder {
        private static final JdbcRegistry jdbcRegistry = new JdbcRegistry(new JdbcOperate());
    }

    public static Registry get() {
        //IRpcConfig rpcConfig = IntegratedFactory.getRpcConfig();
        E4jRpcConfig config = IntegratedFactory.getConfig();
        RegisterType registerType = config.getRegisterType();
        if (RegisterType.JDBC == registerType) {
            return JdbcRegistryHolder.jdbcRegistry;
        } else {
            throw new RuntimeException("not support the type " + config.getRegisterType());
        }
    }

}
