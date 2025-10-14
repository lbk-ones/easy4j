package easy4j.infra.quartz;

import cn.hutool.system.SystemUtil;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.dbaccess.DBAccessFactory;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.IOpMeta;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.TableMetadata;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.sql.Connection;
import java.util.List;
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
        properties.setProperty("spring.quartz.auto-startup", "false");

        // 任务存储类型（使用 JDBC 存储）
        properties.setProperty("spring.quartz.job-store-type", "jdbc");

        String tablePrefix = Easy4j.getProperty("spring.quartz.properties.org.quartz.jobStore.tablePrefix", "QRTZ_");
        String jobStoreType = Easy4j.getProperty("spring.quartz.job-store-type");
        String serverName = getEnvProperty(SysConstant.EASY4J_SERVER_NAME);
        // 默认已经有了
        boolean hasTables = true;
        if (QzConstant.JDBC.equalsIgnoreCase(jobStoreType) || jobStoreType == null) {
            TempDataSource tempDataSource = DBAccessFactory.getTempDataSource();
            Connection quietConnection = tempDataSource.getQuietConnection();
            try {
                DialectV2 select = DialectFactory.get(quietConnection);
                List<TableMetadata> allTableInfoByTableType = select.getAllTableInfoByTableType(tablePrefix + "%", new String[]{"TABLE"});
                if(ListTs.isEmpty(allTableInfoByTableType)){
                    allTableInfoByTableType = select.getAllTableInfoByTableType(tablePrefix.toLowerCase() + "%", new String[]{"TABLE"});
                    if(ListTs.isEmpty(allTableInfoByTableType)){
                        hasTables = false;
                    }
                }
            } finally {
                JdbcHelper.close(quietConnection);
            }
        }

        if(hasTables){
            // JDBC 初始化策略（从不初始化表结构）
            properties.setProperty("spring.quartz.jdbc.initialize-schema", "never");
        }else{
            properties.setProperty("spring.quartz.jdbc.initialize-schema", "always");
        }
        // Quartz 核心属性配置
        // 启用Quartz集群模式（多实例共享任务调度状态）
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.isClustered", "true");
        // 指定JobStore实现类，使用Spring提供的本地数据源JobStore（整合Spring数据源）
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        // 调度器实例ID生成策略：AUTO表示自动生成（集群中每个实例ID唯一）
        properties.setProperty("spring.quartz.properties.org.quartz.scheduler.instanceId", "AUTO");
        // 数据库中Quartz表的前缀（如配置为QRTZ_，则表名会是QRTZ_JOB_DETAILS等）
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.tablePrefix", tablePrefix);
        // 获取触发器时是否在事务锁内操作（集群环境下防止并发获取同一触发器，默认false）
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.acquireTriggersWithinLock", "true");
        // 默认所有线上环境都是linux系统
        if (SystemUtil.getOsInfo().isLinux()) {
            // 调度器实例名称（通常包含服务名，便于集群中区分不同实例）
            properties.setProperty("spring.quartz.properties.org.quartz.scheduler.instanceName", serverName + "-QuartzScheduler");
        }else{
            // 本地单独起一个集群不然会和线上冲突导致触发器ERROR
            String hostName = jodd.util.SystemUtil.info().getHostName();
            System.out.println(SysLog.compact("the env is local so quartz instance name will be set to current hostname " + hostName));
            properties.setProperty("spring.quartz.properties.org.quartz.scheduler.instanceName", hostName + "-QzLocalScheduler");
        }
        // 指定线程池实现类：使用Quartz内置的简单线程池
        properties.setProperty("spring.quartz.properties.org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        // 是否将线程池中的线程设置为守护线程（守护线程会随JVM退出而终止，默认false）
        properties.setProperty("spring.quartz.properties.org.quartz.threadPool.makeThreadsDaemons", "true");
        // 线程池中线程的优先级（1-10，默认5，数值越高优先级越高）
        properties.setProperty("spring.quartz.properties.org.quartz.threadPool.threadPriority", "5");
        // 线程池核心线程数（同时可执行的任务最大并发数，根据业务调整）
        properties.setProperty("spring.quartz.properties.org.quartz.threadPool.threadCount", "25");
        // 是否将JobDataMap中的数据以Properties格式存储（false表示以序列化对象存储，默认false）
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.useProperties", "false");
        // 任务错过触发的阈值（单位：毫秒），超过此时长视为"错过触发"，将按misfire策略处理
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.misfireThreshold", "60000");
        // 是否将调度器主线程设置为守护线程（默认false）
        properties.setProperty("spring.quartz.properties.org.quartz.scheduler.makeSchedulerThreadDaemon", "true");
        // 根据数据库类型指定对应的驱动代理类（如MySQL用StdJDBCDelegate，Oracle用OracleDelegate等）
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.driverDelegateClass", getDriverDelegateByDatabase());
        // 集群中实例的心跳检查间隔（单位：毫秒），用于检测实例是否存活
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval", "5000");
        // 每次批量获取触发器的最大数量（集群环境下控制并发获取触发器的数量，默认1）
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
