package easy4j.infra.rpc.integrated;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 连接管理
 *
 * @author bokun
 * @since 2.0.1
 */
public interface ConnectionManager {

    /**
     * getDataSource
     *
     * @return DataSource
     */
    DataSource getDataSource();

    /**
     * getConnection
     *
     * @return Connection
     */
    Connection getConnection() throws SQLException;

    /**
     * releaseConnection
     */
    void releaseConnection(Connection connection);


}
