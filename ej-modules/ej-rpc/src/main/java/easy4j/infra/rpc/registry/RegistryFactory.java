package easy4j.infra.rpc.registry;

import easy4j.infra.rpc.config.BaseConfig;
import easy4j.infra.rpc.enums.RegisterType;
import easy4j.infra.rpc.registry.jdbc.JdbcOperate;
import easy4j.infra.rpc.registry.jdbc.JdbcRegistry;

/**
 * 获取注册中心
 *
 * @author bokun
 * @since 2.0.1
 */
public class RegistryFactory {


    public static Registry get(BaseConfig baseConfig) {
        RegisterType registerType = baseConfig.getRegisterType();
        if (RegisterType.JDBC == registerType) {
            return new JdbcRegistry(new JdbcOperate(baseConfig));
        } else {
            throw new RuntimeException("not support the type " + baseConfig.getRegisterType());
        }
    }

}
