package easy4j.infra.rpc.integrated;

import cn.hutool.db.dialect.DialectFactory;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.utils.RpcJdbcTempDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
@Slf4j
public class DefaultConnectionManager implements ConnectionManager{


    public DefaultConnectionManager() {
        E4jRpcConfig rpcConfig = IntegratedFactory.getRpcConfig().getConfig();
        String registryJdbcUrl =rpcConfig.getRegistryJdbcUrl();
        String registryJdbcUsername = rpcConfig.getRegistryJdbcUsername();
        String registryJdbcPassword = rpcConfig.getRegistryJdbcPassword();
        log.info("registry jdbc url {}",registryJdbcUrl);
        log.info("registry jdbc username {}",registryJdbcUsername);
        log.info("registry jdbc password {}",registryJdbcPassword);
    }

    @Override
    public DataSource getDataSource() {
        E4jRpcConfig rpcConfig = IntegratedFactory.getRpcConfig().getConfig();
        String s = DialectFactory.identifyDriver(rpcConfig.getRegistryJdbcUrl());
        return new RpcJdbcTempDataSource(s, rpcConfig.getRegistryJdbcUrl(), rpcConfig.getRegistryJdbcUsername(), rpcConfig.getRegistryJdbcPassword());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    @Override
    public void releaseConnection(Connection connection) {
        if(connection != null){
            try {
                connection.close();
            } catch (SQLException ignored) {

            }
        }
    }
}
