package easy4j.module.base.utils;

import jodd.util.StringPool;

public class SysConstant {

    public static final String PARAM_PREFIX = "easy4j";

    public static final String X_ACCESS_TOKEN = "X-Access-Token";
    public static final String X_SIGN = "X-Sign";
    public static final String X_TIMESTAMP = "X-TIMESTAMP";
    public static final String TENANT_ID = "X-Tenant-Id";


    public static final String DOT = ".";

    public static final String DB_URL_STR = "spring.datasource.url";
    public static final String DB_DATASOURCE_TYPE = "spring.datasource.type";
    public static final String DB_URL_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";
    public static final String DB_USER_NAME = "spring.datasource.username";
    public static final String DB_USER_PASSWORD = "spring.datasource.password";
    public static final String SPRING_SERVER_PORT = "server.port";
    public static final String SPRING_SERVER_NAME = "spring.application.name";
    public static final String SERVER_PORT_STR = "server.port";
    public static final String DRUID_USER_NAME = "spring.datasource.druid.stat-view-servlet.login-username";
    public static final String DRUID_USER_PWD = "spring.datasource.druid.stat-view-servlet.login-password";
    public static final String DRUID_FILTER = "spring.datasource.druid.filters";
    public static final String SPRING_PROFILE_ACTIVE = "spring.profiles.active";
    public static final String SPRING_PROFILE_INCLUDES = "spring.profiles.include";
    public static final String SPRING_CONFIG_IMPORT = "spring.config.import";
    // ------spring cloud alibaba begin
    public static final String SPRING_CLOUD_NACOS_URL = "spring.cloud.nacos.server-addr";
    public static final String SPRING_CLOUD_NACOS_DISCOVERY = "spring.cloud.nacos.discovery";
    public static final String SPRING_CLOUD_NACOS_DISCOVERY_GROUP = "spring.cloud.nacos.discovery.group";
    public static final String SPRING_CLOUD_NACOS_DISCOVERY_NAMESPACE = "spring.cloud.nacos.discovery.namespace";
    public static final String SPRING_CLOUD_NACOS_DISCOVERY_URL = "spring.cloud.nacos.discovery.server-addr";
    public static final String SPRING_CLOUD_NACOS_DISCOVERY_USERNAME = "spring.cloud.nacos.discovery.username";
    public static final String SPRING_CLOUD_NACOS_DISCOVERY_PASSWORD = "spring.cloud.nacos.discovery.password";
    public static final String SPRING_CLOUD_NACOS_CONFIG = "spring.cloud.nacos.config";
    public static final String SPRING_CLOUD_NACOS_CONFIG_GROUP = "spring.cloud.nacos.config.group";
    public static final String SPRING_CLOUD_NACOS_CONFIG_NAMESPACE = "spring.cloud.nacos.config.namespace";
    public static final String SPRING_CLOUD_NACOS_CONFIG_URL = "spring.cloud.nacos.config.server-addr";
    public static final String SPRING_CLOUD_NACOS_CONFIG_USERNAME = "spring.cloud.nacos.config.username";
    public static final String SPRING_CLOUD_NACOS_CONFIG_PASSWORD = "spring.cloud.nacos.config.password";
    public static final String SPRING_CLOUD_NACOS_CONFIG_FILE_EXTENSION = "spring.cloud.nacos.config.file-extension";
    public static final String SPRING_CLOUD_NACOS_USERNAME = "spring.cloud.nacos.username";
    public static final String SPRING_CLOUD_NACOS_PASSWORD = "spring.cloud.nacos.password";
    public static final String SPRING_CLOUD_NACOS_SERVER_ADDR = "spring.cloud.nacos.server-addr";
    // ------spring cloud alibaba end

    // -------------------------EASY4j PROPERTIES----------------------------------------

    public static final String DB_URL_STR_NEW = PARAM_PREFIX + StringPool.DOT + "data-source-url";

    public static final String LOG_SAVE_PATH = PARAM_PREFIX + StringPool.DOT + "log-save-path";
    public static final String SIGNATURE_SECRET = PARAM_PREFIX + StringPool.DOT + "signature-secret";
    public static final String SIGN_URLS = PARAM_PREFIX + StringPool.DOT + "sign-urls";
    public static final String GLOBAL_CORS_ENABLE = PARAM_PREFIX + StringPool.DOT + "cors-reject-enable";
    public static final String SEED_IP_SEGMENT = PARAM_PREFIX + StringPool.DOT + "seed-ip-segment";
    public static final String H2_ENABLE = PARAM_PREFIX + StringPool.DOT + "h2-enable";
    public static final String H2_URL = PARAM_PREFIX + StringPool.DOT + "h2-url";
    public static final String H2_USER_NAME = PARAM_PREFIX + StringPool.DOT + "h2-console-username";
    public static final String H2_PASSWORD = PARAM_PREFIX + StringPool.DOT + "h2-console-password";
    public static final String AUTHOR = PARAM_PREFIX + StringPool.DOT + "author";

    public static final String EASY4J_SERVER_PORT = PARAM_PREFIX + StringPool.DOT + "server-port";
    public static final String EASY4J_SERVER_NAME = PARAM_PREFIX + StringPool.DOT + "server-name";
    public static final String EASY4J_SERVICE_DESC = PARAM_PREFIX + StringPool.DOT + "server-desc";
    public static final String EASY4J_CONFIG_IMPORT = PARAM_PREFIX + StringPool.DOT + "config-import";
    public static final String EASY4J_SCA_ENABLE = PARAM_PREFIX + StringPool.DOT + "enable-sca";
    public static final String EASY4J_SCA_ENV = PARAM_PREFIX + StringPool.DOT + "env";


    public static final String EASY4J_SCA_NACOS_URL = PARAM_PREFIX + StringPool.DOT + "nacos-url";
    public static final String EASY4J_SCA_NACOS_USERNAME = PARAM_PREFIX + StringPool.DOT + "nacos-username";
    public static final String EASY4J_SCA_NACOS_PASSWORD = PARAM_PREFIX + StringPool.DOT + "nacos-password";
    public static final String EASY4J_SCA_NACOS_CONFIG_URL = PARAM_PREFIX + StringPool.DOT + "nacos-config-url";
    public static final String EASY4J_SCA_NACOS_CONFIG_USERNAME = PARAM_PREFIX + StringPool.DOT + "nacos-config-username";
    public static final String EASY4J_SCA_NACOS_CONFIG_PASSWORD = PARAM_PREFIX + StringPool.DOT + "nacos-config-password";
    public static final String EASY4J_SCA_NACOS_CONFIG_GOURP = PARAM_PREFIX + StringPool.DOT + "nacos-config-group";
    public static final String EASY4J_SCA_NACOS_CONFIG_NAMESPACE = PARAM_PREFIX + StringPool.DOT + "nacos-config-namespace";
    public static final String EASY4J_SCA_NACOS_DISCOVERY_URL = PARAM_PREFIX + StringPool.DOT + "nacos-discovery-url";
    public static final String EASY4J_SCA_NACOS_DISCOVERY_USERNAME = PARAM_PREFIX + StringPool.DOT + "nacos-discovery-username";
    public static final String EASY4J_SCA_NACOS_DISCOVERY_PASSWORD = PARAM_PREFIX + StringPool.DOT + "nacos-discovery-password";
    public static final String EASY4J_SCA_NACOS_DISCOVERY_GROUP = PARAM_PREFIX + StringPool.DOT + "nacos-discovery-group";
    public static final String EASY4J_SCA_NACOS_DISCOVERY_NAMESPACE = PARAM_PREFIX + StringPool.DOT + "nacos-discovery-namespace";
    public static final String EASY4J_SCA_FILE_EXTENSION = PARAM_PREFIX + StringPool.DOT + "nacos-config-file-extension";
    public static final String EASY4J_NACOS_DATA_IDS = PARAM_PREFIX + StringPool.DOT + "data-ids";
    public static final String EASY4J_NACOS_GROUP = PARAM_PREFIX + StringPool.DOT + "nacos-group";
    public static final String EASY4J_NACOS_NAMESPACE = PARAM_PREFIX + StringPool.DOT + "nacos-namespace";

    public static final String EASY4J_SECURITY_OLD_SCHOOL = PARAM_PREFIX + StringPool.DOT + "security-old-school-enable";
    public static final String EASY4J_SAUTH_ENABLE = PARAM_PREFIX + StringPool.DOT + "simple-auth-enable";

    // -------------------------EASY4j PROPERTIES----------------------------------------
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
    public static final String SPRING_JPA_DATABASE_PLATFORM = "spring.jpa.database-platform";

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
    public static final String SPRING_JPA_GENERATE_DDL = "spring.jpa.generate-ddl";
    public static final String SPRING_JPA_HIBERNATE_DDL_AUTO = "spring.jpa.hibernate.ddl-auto";


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


    /**
     * knife4j 整合相关
     */
    public static final String KNIFE4J_ENABLE = "knife4j.enable";
    public static final String KNIFE4J_SETTING_LANGUAGE = "knife4j.setting.language";
    public static final String KNIFE4J_SETTING_ENABLE_SWAGGER_MODELS = "knife4j.setting.enableSwaggerModels";
    public static final String KNIFE4J_SETTING_ENABLE_DOCUMENT_MANAGE = "knife4j.setting.enableDocumentManage";
    public static final String KNIFE4J_SETTING_SWAGGER_MODEL_NAME = "knife4j.setting.swaggerModelName";
    public static final String KNIFE4J_SETTING_ENABLE_VERSION = "knife4j.setting.enableVersion";
    public static final String KNIFE4J_SETTING_ENABLE_RELOAD_CACHE_PARAMETER = "knife4j.setting.enableReloadCacheParameter";
    public static final String KNIFE4J_SETTING_ENABLE_AFTER_SCRIPT = "knife4j.setting.enableAfterScript";
    public static final String KNIFE4J_SETTING_ENABLE_FILTER_MULTIPART_API_METHOD_TYPE = "knife4j.setting.enableFilterMultipartApiMethodType";
    public static final String KNIFE4J_SETTING_ENABLE_FILTER_MULTIPART_APIS = "knife4j.setting.enableFilterMultipartApis";
    public static final String KNIFE4J_SETTING_ENABLE_REQUEST_CACHE = "knife4j.setting.enableRequestCache";
    public static final String KNIFE4J_SETTING_ENABLE_HOST = "knife4j.setting.enableHost";
    public static final String KNIFE4J_SETTING_ENABLE_HOST_TEXT = "knife4j.setting.enableHostText";
    public static final String KNIFE4J_SETTING_ENABLE_HOME_CUSTOM = "knife4j.setting.enableHomeCustom";
    public static final String KNIFE4J_SETTING_HOME_CUSTOM_LOCATION = "knife4j.setting.homeCustomLocation";
    public static final String KNIFE4J_SETTING_ENABLE_SEARCH = "knife4j.setting.enableSearch";
    public static final String KNIFE4J_SETTING_ENABLE_FOOTER = "knife4j.setting.enableFooter";
    public static final String KNIFE4J_SETTING_ENABLE_FOOTER_CUSTOM = "knife4j.setting.enableFooterCustom";
    public static final String KNIFE4J_SETTING_FOOTER_CUSTOM_CONTENT = "knife4j.setting.footerCustomContent";
    public static final String KNIFE4J_SETTING_ENABLE_DYNAMIC_PARAMETER = "knife4j.setting.enableDynamicParameter";
    public static final String KNIFE4J_SETTING_ENABLE_DEBUG = "knife4j.setting.enableDebug";
    public static final String KNIFE4J_SETTING_ENABLE_OPEN_API = "knife4j.setting.enableOpenApi";
    public static final String KNIFE4J_SETTING_ENABLE_GROUP = "knife4j.setting.enableGroup";
    public static final String KNIFE4J_CORS = "knife4j.cors";
    public static final String KNIFE4J_PRODUCTION = "knife4j.production";
    public static final String KNIFE4J_BASIC_ENABLE = "knife4j.basic.enable";
    public static final String KNIFE4J_BASIC_USERNAME = "knife4j.basic.username";
    public static final String KNIFE4J_BASIC_PASSWORD = "knife4j.basic.password";


}
