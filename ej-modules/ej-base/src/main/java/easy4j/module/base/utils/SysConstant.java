package easy4j.module.base.utils;

public class SysConstant {
    public static final String DOT = ".";

    public static final String DB_URL_STR = "spring.datasource.url";
    public static final String DB_DATASOURCE_TYPE = "spring.datasource.type";
    public static final String DB_URL_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";
    public static final String DB_USER_NAME = "spring.datasource.username";
    public static final String DB_USER_PASSWORD = "spring.datasource.password";
    public static final String DB_URL_STR_NEW = "ej.datasource.url";
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_NAME = "spring.application.name";
    public static final String LOG_SAVE_PATH = "log.save.path";
    public static final String  SERVER_PORT_STR = "server.port";
    public static final String  DRUID_USER_NAME = "spring.datasource.druid.stat-view-servlet.login-username";
    public static final String  DRUID_USER_PWD = "spring.datasource.druid.stat-view-servlet.login-password";
    public static final String  DRUID_FILTER = "spring.datasource.druid.filters";


    public static final int SUCCESSCODE = 0;

    public static final int ERRORCODE = 1;

    public static final String CHINESESUCCESS = "操作成功";

    public static final String CHINESEERRORMSG = "操作失败";


    public static final String CHINESSYSERROR = "系统错误";


    public static final String ENUMS = "enums";
    public static final String DOMAINS = "domains";
    public static final String XML_LOCATION = "classpath*:/mapper/**/*.xml";

    // jpa
    /**
     * 对应 Spring JPA 配置中数据库类型的配置项键
     */
    public static final String SPRING_JPA_DATABASE = "spring.jpa.database";

    /**
     * 对应 Spring JPA 配置中是否显示 SQL 语句的配置项键
     */
    public static final String SPRING_JPA_SHOW_SQL = "spring.jpa.show-sql";

    /**
     * 对应 Spring JPA 中 Hibernate 格式化 SQL 语句配置项的键
     */
    public static final String SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL = "spring.jpa.properties.hibernate.format_sql";
    // 生成 sql 注释 类似于这种  /* load com.example.Entity */
    public static final String SPRING_JPA_PROPERTIES_HIBERNATE_USE_SQL_COMMENTS = "spring.jpa.properties.hibernate.use_sql_comments";

    /**
     * 对应 Spring JPA 中 Hibernate 物理命名策略配置项的键
     */
    public static final String SPRING_JPA_HIBERNATE_NAMING_PHYSICAL_STRATEGY = "spring.jpa.hibernate.naming.physical-strategy";

    /**
     * 对应 Spring JPA 中 Hibernate JDBC 批量操作大小配置项的键
     */
    public static final String SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE = "spring.jpa.properties.hibernate.jdbc.batch_size";

    /**
     * 对应 Spring JPA 中 Hibernate 数据库方言配置项的键
     */
    public static final String SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT = "spring.jpa.properties.hibernate.dialect";

    /**
     * 对应 Spring JPA 中 Hibernate 当前会话上下文类配置项的键
     */
    public static final String SPRING_JPA_PROPERTIES_HIBERNATE_CURRENT_SESSION_CONTEXT_CLASS = "spring.jpa.properties.hibernate.current_session_context_class";

    /**
     * 对应 Spring JPA 中 Hibernate 数据库表结构自动更新策略配置项的键
     */
    public static final String SPRING_JPA_PROPERTIES_HIBERNATE_HBM2DDL_AUTO = "spring.jpa.properties.hibernate.hbm2ddl.auto";


    // h2

    /**
     * 对应 Spring 数据源的 URL 配置项的键
     */
    /**
     * 对应是否开启 H2 控制台配置项的键
     */
    public static final String SPRING_H2_CONSOLE_ENABLED = "spring.h2.console.enabled";
    /**
     * 对应 H2 控制台路径配置项的键
     */
    public static final String SPRING_H2_CONSOLE_PATH = "spring.h2.console.path";
}
