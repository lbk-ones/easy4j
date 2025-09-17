package easy4j.infra.quartz;

import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.SysConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class Environment extends AbstractEasy4jEnvironment {

    @Override
    public String getName() {
        return "easy4j-quartz-environment";
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        try {
            Class.forName("org.quartz.Scheduler");
        } catch (ClassNotFoundException e) {
            return null;
        }

        // Quartz 自动启动设置
        properties.setProperty("spring.quartz.auto-startup", "true");

        // 任务存储类型（使用 JDBC 存储）
        properties.setProperty("spring.quartz.job-store-type", "jdbc");

        // JDBC 初始化策略（从不初始化表结构）
        properties.setProperty("spring.quartz.jdbc.initialize-schema", "never");
        String serverName = getEnvProperty(SysConstant.EASY4J_SERVER_NAME);

        // Quartz 核心属性配置
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.isClustered", "true");
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        properties.setProperty("spring.quartz.properties.org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.acquireTriggersWithinLock", "true");
        properties.setProperty("spring.quartz.properties.org.quartz.scheduler.instanceName", serverName + "Scheduler");
        properties.setProperty("spring.quartz.properties.org.quartz.threadPool.class", "org.apache.dolphinscheduler.scheduler.quartz.QuartzZeroSizeThreadPool");
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.useProperties", "false");
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.misfireThreshold", "60000");
        properties.setProperty("spring.quartz.properties.org.quartz.scheduler.makeSchedulerThreadDaemon", "true");
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.driverDelegateClass", getDriverDelegateByDatabase());
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval", "5000");
        properties.setProperty("spring.quartz.properties.org.quartz.scheduler.batchTriggerAcquisitionMaxCount", "1");

        return properties;
    }

    private String getDriverDelegateByDatabase() {
        String dbType = getDbType().toLowerCase();
        // 获取数据库连接元数据
        // 根据数据库产品名称匹配对应的DriverDelegate
        if (DbType.POSTGRE_SQL.getDb().equals(dbType)) {
            return "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
        } else if (DbType.ORACLE.getDb().equals(dbType)) {
            return "org.quartz.impl.jdbcjobstore.OracleDelegate";
        } else if (DbType.SQL_SERVER.getDb().equals(dbType)) {
            return "org.quartz.impl.jdbcjobstore.MSSQLDelegate";
        } else if (DbType.DB2.getDb().equals(dbType)) {
            return "org.quartz.impl.jdbcjobstore.DB2v6Delegate";
        } else {
            // 默认使用通用 delegate（可能存在兼容性问题）
            return "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
        }
    }


    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
