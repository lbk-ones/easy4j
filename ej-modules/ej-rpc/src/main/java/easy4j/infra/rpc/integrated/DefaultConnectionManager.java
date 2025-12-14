package easy4j.infra.rpc.integrated;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.dialect.DialectFactory;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.utils.RpcJdbcTempDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 没有则数据源降级使用这个
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class DefaultConnectionManager implements ConnectionManager {

    public static final DefaultConnectionManager INSTANCE = new DefaultConnectionManager();

    DataSource dataSource;

    public DefaultConnectionManager() {
    }

    @Override
    public DataSource getDataSource() {
        if (dataSource == null) {
            E4jRpcConfig rpcConfig = IntegratedFactory.getConfig();
            String registryJdbcUrl = rpcConfig.getRegistryJdbcUrl();
            String registryJdbcUsername = rpcConfig.getRegistryJdbcUsername();
            String registryJdbcPassword = rpcConfig.getRegistryJdbcPassword();
            if (StrUtil.hasBlank(registryJdbcUrl, registryJdbcUsername, registryJdbcPassword)) {
                throw new RpcException("RPC not loaded yet, please switch scenarios to call");
            }
            String s = DialectFactory.identifyDriver(registryJdbcUrl);
            if (s == null) {
                throw new RpcException("not identify driver :" + registryJdbcUrl);
            }
            dataSource = new RpcJdbcTempDataSource(s, registryJdbcUrl, registryJdbcUsername, registryJdbcPassword);
        }
        return dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {

            }
        }
    }
}
