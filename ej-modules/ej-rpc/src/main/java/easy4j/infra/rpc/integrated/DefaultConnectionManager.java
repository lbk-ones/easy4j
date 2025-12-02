package easy4j.infra.rpc.integrated;

import cn.hutool.db.dialect.DialectFactory;
import easy4j.infra.rpc.config.BaseConfig;
import easy4j.infra.rpc.utils.RpcJdbcTempDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
@Slf4j
public class DefaultConnectionManager implements ConnectionManager{

    final BaseConfig baseConfig;

    public DefaultConnectionManager(BaseConfig baseConfig) {
        this.baseConfig = baseConfig;
        String registryJdbcUrl = this.baseConfig.getRegistryJdbcUrl();
        String registryJdbcUsername = this.baseConfig.getRegistryJdbcUsername();
        String registryJdbcPassword = this.baseConfig.getRegistryJdbcPassword();
        log.info("registry jdbc url {}",registryJdbcUrl);
        log.info("registry jdbc username {}",registryJdbcUsername);
        log.info("registry jdbc password {}",registryJdbcPassword);
    }

    @Override
    public DataSource getDataSource() {
        String s = DialectFactory.identifyDriver(baseConfig.getRegistryJdbcUrl());
        return new RpcJdbcTempDataSource(s, baseConfig.getRegistryJdbcUrl(), baseConfig.getRegistryJdbcUsername(), baseConfig.getRegistryJdbcPassword());
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
