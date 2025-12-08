package easy4j.infra.rpc.integrated.spring;

import easy4j.infra.rpc.integrated.ConnectionManager;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 获取连接的方式，整合spring实现
 */
public class SpringConnectionManager implements ConnectionManager, ApplicationContextAware, BeanNameAware {
    ApplicationContext context;

    String beanName;

    volatile DataSource dataSource;

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


    public DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (SpringConnectionManager.class) {
                if (dataSource == null) {
                    dataSource = context.getBean(DataSource.class);
                }
            }
        }
        return dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    @Override
    public void releaseConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, getDataSource());
    }
}
