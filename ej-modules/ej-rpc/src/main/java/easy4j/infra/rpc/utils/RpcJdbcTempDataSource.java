package easy4j.infra.rpc.utils;

import easy4j.infra.rpc.exception.SqlRuntimeException;
import lombok.Getter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 临时的DataSource实现，每次获取连接都通过JDBC驱动直接创建新连接
 *
 * @author bokun.li
 * @date 2025-09-13
 */

public class RpcJdbcTempDataSource implements DataSource {

    @Getter
    private final String driverClass;
    @Getter
    private final String url;
    @Getter
    private final String username;
    @Getter
    private final String password;
    private final Properties connectionProperties;

    private PrintWriter logWriter;
    private int loginTimeout;

    /**
     * 构造函数
     *
     * @param driverClass JDBC驱动类名
     * @param url         数据库连接URL
     * @param username    数据库用户名
     * @param password    数据库密码
     * @throws ClassNotFoundException 如果驱动类未找到
     */
    public RpcJdbcTempDataSource(String driverClass, String url, String username, String password) {
        this.driverClass = driverClass;
        this.url = url;
        this.username = username;
        this.password = password;

        // 加载JDBC驱动
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // 初始化连接属性
        this.connectionProperties = new Properties();
        if (username != null) {
            this.connectionProperties.setProperty("user", username);
        }
        if (password != null) {
            this.connectionProperties.setProperty("password", password);
        }

        // 初始化默认值
        this.logWriter = null;
        this.loginTimeout = 0;
    }

    /**
     * 获取数据库连接
     * 每次调用都会通过JDBC驱动创建新的连接
     */
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, connectionProperties);
    }

    /**
     * 使用指定的用户名和密码获取连接
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        return DriverManager.getConnection(url, props);
    }

    /**
     * 获取日志输出流
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    /**
     * 设置日志输出流
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    /**
     * 设置登录超时时间（秒）
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
        DriverManager.setLoginTimeout(seconds);
    }

    /**
     * 获取登录超时时间
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    /**
     * 获取父日志记录器
     */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger is not supported");
    }

    /**
     * 类型转换方法
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException("DataSource does not support unwrapping to " + iface.getName());
    }

    /**
     * 检查是否可以转换为指定类型
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    /**
     * 安静的获取连接 不申明异常
     *
     * @return Connection
     */
    public Connection getQuietConnection() {
        try {
            return getConnection();
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        }
    }


}
