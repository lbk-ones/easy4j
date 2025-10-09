/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.common.utils;

import jodd.util.StringPool;

/**
 * SysConstant
 *
 * @author bokun.li
 * @date 2025-05
 */
public class SysConstant {

    public static final String PARAM_PREFIX = "easy4j";
    // 包结构的前缀
    public static final String PACKAGE_PREFIX = PARAM_PREFIX;

    public static final String X_ACCESS_TOKEN = "X-Access-Token";
    public static final String X_SIGN = "X-Sign";
    public static final String X_TIMESTAMP = "X-TIMESTAMP";
    public static final String X_TENANT_ID = "X-Tenant-Id";
    public static final String TRACE_ID_NAME = "traceId";
    public static final String AUTHORIZATION = "Authorization";
    public static final String AUTHORIZATION_TYPE = "AuthorizationType";

    // OpenTracing协议: uber-trace-id
    // W3C Trace Context协议: traceparent
    // B3 协议: 	X-B3-TraceId	X-B3-SpanId	X-B3-ParentSpanId	X-B3-Sampled
    // Jaeger 协议: uber-b3-id
    public static final String SERVER_TRACE_NAME = "uber-trace-id";

    public static final String X_API_KEY = "X-API-Key";
    public static final String EASY4J_NO_NEED_TOKEN = "X-Skip-Token";


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
    public static final String EASY4J_DEV = PARAM_PREFIX + StringPool.DOT + "dev";


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
    public static final String EASY4J_SAUTH_IS_SERVER = PARAM_PREFIX + StringPool.DOT + "simple-auth-is-server";
    public static final String EASY4J_PRINT_REQUEST_LOG = PARAM_PREFIX + StringPool.DOT + "print-request-log";
    public static final String EASY4J_AUTH_SESSION_STORAGE_TYPE = PARAM_PREFIX + StringPool.DOT + "simple-auth-session-storage-type";
    public static final String EASY4J_AUTH_SESSION_EXPIRE_TIME = PARAM_PREFIX + StringPool.DOT + "session-expire-time-seconds";
    public static final String EASY4J_SIMPLE_AUTH_ENABLE = PARAM_PREFIX + StringPool.DOT + "simple-auth-enable";
    public static final String EASY4J_SIMPLE_AUTH_IS_SERVER = PARAM_PREFIX + StringPool.DOT + "simple-auth-is-server";
    public static final String EASY4J_SIMPLE_AUTH_SCAN_PACKAGE_PREFIX = PARAM_PREFIX + StringPool.DOT + "simple-auth-scan-package-prefix";
    public static final String EASY4J_SIMPLE_AUTH_USERNAME = PARAM_PREFIX + StringPool.DOT + "simple-auth-username";
    public static final String EASY4J_SIMPLE_AUTH_USERNAME_CN = PARAM_PREFIX + StringPool.DOT + "simple-auth-username-cn";

    // 用户信息的实现类型（default、extra）default代表默认实现（默认实现会自动建表），extra代表是外部业务实现，如果是extra则不建默认用户表：该字段无默认值如果开启了EASY4J_SAUTH_IS_SERVER那么必须设置
    public static final String EASY4J_SIMPLE_AUTH_USER_IMPL_TYPE = PARAM_PREFIX + StringPool.DOT + "simple-auth-user-impl-type";
    public static final String EASY4J_SIMPLE_AUTH_REGIST_TO_NACOS = PARAM_PREFIX + StringPool.DOT + "simple-auth-register-to-nacos";
    public static final String EASY4J_SIMPLE_AUTH_IS_CACHE_AUTHORITY = PARAM_PREFIX + StringPool.DOT + "simple-auth-is-cache-authority";

    public static final String EASY4J_SIMPLE_AUTH_PASSWORD = PARAM_PREFIX + StringPool.DOT + "simple-auth-password";
    public static final String EASY4J_SIMPLE_AUTH_SESSION_REPEAT_STRATEGY = PARAM_PREFIX + StringPool.DOT + "simple-auth-session-repeat-strategy";
    public static final String EASY4J_ENABLE_DB_REQUEST_LOG = PARAM_PREFIX + StringPool.DOT + "db-request-log-enable";


    public static final String EASY4J_ENABLE_PRINT_SYS_DB_SQL = PARAM_PREFIX + StringPool.DOT + "enable-print-sys-db-sql";

    public static final String EASY4J_CACHE_CONTENT_LENGTH = PARAM_PREFIX + StringPool.DOT + "cache-http-content-length";

    public static final String EASY4J_BOOT_ADMIN_SERVER_URL = PARAM_PREFIX + StringPool.DOT + "admin-server-url";

    public static final String EASY4J_REDIS_URL = PARAM_PREFIX + StringPool.DOT + "redis-server-url";
    // 代表是否启用了redis模块
    public static final String EASY4J_REDIS_ENABLE = PARAM_PREFIX + StringPool.DOT + "redis-enable";
    public static final String EASY4J_REDIS_CONNECTION_TYPE = PARAM_PREFIX + StringPool.DOT + "redis-connection-type";
    public static final String EASY4J_FLYWAY_ENABLE = PARAM_PREFIX + StringPool.DOT + "flyway-enable";
    public static final String EASY4J_FLYWAY_CHECKSUM_DISABLED = PARAM_PREFIX + SP.DOT + "flyway.checksum.disabled";
    public static final String EASY4J_SEATA_ENABLE = PARAM_PREFIX + StringPool.DOT + "seata-enable";
    public static final String EASY4J_SEATA_TX_GROUP = PARAM_PREFIX + StringPool.DOT + "seata-tx-group";

    public static final String EASY4J_SEATA_REGISTRY_TYPE = PARAM_PREFIX + StringPool.DOT + "seata-registry-type";
    public static final String EASY4J_SEATA_NACOS_URL = PARAM_PREFIX + StringPool.DOT + "seata-nacos-url";
    public static final String EASY4J_SEATA_NACOS_CLUSTER = PARAM_PREFIX + StringPool.DOT + "seata-nacos-cluster";
    public static final String EASY4J_SEATA_NACOS_GROUP = PARAM_PREFIX + StringPool.DOT + "seata-nacos-group";

    public static final String EASY4J_XXLJOB_ENABLE = PARAM_PREFIX + SP.DOT + "xxl-job-enable";
    public static final String EASY4J_XXLJOB_ADMIN_URL = PARAM_PREFIX + SP.DOT + "xxl-job-admin-url";
    public static final String EASY4J_XXLJOB_ACCESS_TOKEN = PARAM_PREFIX + SP.DOT + "xxl-job-access-token";
    public static final String EASY4J_METRICS_ENABLE = PARAM_PREFIX + SP.DOT + "metrics-enable";
    public static final String EASY4J_DEFAULT_I18N = PARAM_PREFIX + SP.DOT + "default-i18n";
    public static final String EASY4J_DB_ACCESS_NOT_CACHE_SCHEMA = PARAM_PREFIX + SP.DOT + "db-access-not-cache-schema";

    public static final String EASY4J_LOG_PATH = PARAM_PREFIX + SP.DOT + "log-path";
    public static final String EASY4J_MINIO_URL = PARAM_PREFIX + SP.DOT + "minio-url";
    public static final String EASY4J_MINIO_ACCESS_KEY = PARAM_PREFIX + SP.DOT + "minio-access-key";
    public static final String EASY4J_MINIO_SECRET_KEY = PARAM_PREFIX + SP.DOT + "minio-secret-key";
    public static final String EASY4J_IS_GLOBAL_PRINT_LOG = PARAM_PREFIX + SP.DOT + "global-quartz-job-print-log";
    public static final String EASY4J_FORCE_REGISTER_TO_REGISTRY = PARAM_PREFIX + SP.DOT + "force-register-to-registry";
    public static final String EASY4J_QUARTZ_JOB_RESTART_CHECK_DELETE = PARAM_PREFIX + SP.DOT + "quartz-job-restart-check-delete";



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
    public static final String SPRING_BOOT_ADMIN_ENABLE = "spring.application.admin.enabled";
    public static final String SPRING_BOOT_ADMIN_URL = "spring.boot.admin.client.url";
    public static final String SPRING_BOOT_ADMIN_USERNAME = "spring.boot.admin.client.username";
    public static final String SPRING_BOOT_ADMIN_PASSWORD = "spring.boot.admin.client.password";
    public static final String SPRING_BOOT_ADMIN_HOST_TYPE = "spring.boot.admin.client.instance.service-host-type";
    public static final String SPRING_BOOT_ADMIN_CONNECTION_TIMEOUT = "spring.boot.admin.client.connect-timeout";
    public static final String SPRING_BOOT_ADMIN_READ_TIMEOUT = "spring.boot.admin.client.read-timeout";
    public static final String SPRING_REDIS_URL = "spring.redis.url";
    public static final String SPRING_REDIS_HOST = "spring.redis.host";
    public static final String SPRING_REDIS_PORT = "spring.redis.port";
    public static final String SPRING_REDIS_USERNAME = "spring.redis.username";
    public static final String SPRING_REDIS_PASSWORD = "spring.redis.password";
    public static final String SPRING_REDIS_SENTINEL_MASTER = "spring.redis.sentinel.master";
    public static final String SPRING_REDIS_SENTINEL_NODES = "spring.redis.sentinel.nodes";
    public static final String SPRING_REDIS_SENTINEL_USERNAME = "spring.redis.sentinel.username";
    public static final String SPRING_REDIS_SENTINEL_PASSWORD = "spring.redis.sentinel.password";
    public static final String SPRING_REDIS_CLUSTER_NODES = "spring.redis.cluster.nodes";
    public static final String SPRING_REDIS_CLUSTER_MAX_REDIRECTS = "spring.redis.cluster.max-redirects";
    // read timeout
    public static final String SPRING_REDIS_TIMEOUT = "spring.redis.timeout";
    // connection_timeout
    public static final String SPRING_REDIS_CONNECT_TIMEOUT = "spring.redis.connect-timeout";
    public static final String SPRING_REDIS_DATABASE = "spring.redis.database";
    public static final String SPRING_REDIS_SSL = "spring.redis.ssl";
    public static final String SPRING_REDIS_LETTUCE_POOL_ENABLE = "spring.redis.lettuce.pool.enabled";
    public static final String SPRING_REDIS_LETTUCE_POOL_MAX_ACTIVE = "spring.redis.lettuce.pool.max-active";

    // 最大空闲数
    public static final String SPRING_REDIS_LETTUCE_POOL_MAX_IDLE = "spring.redis.lettuce.pool.max-idle";
    // 最小空闲数
    public static final String SPRING_REDIS_LETTUCE_POOL_MIN_IDLE = "spring.redis.lettuce.pool.min-idle";
    // 关闭连接的超时
    public static final String SPRING_REDIS_LETTUCE_POOL_SHUTDOWN_TIMEOUT = "spring.redis.lettuce.shutdown-timeout";

    // 获取连接的最大等待时间
    public static final String SPRING_REDIS_LETTUCE_POOL_MAX_WAIT = "spring.redis.lettuce.pool.max-wait";


    // h2

    /**
     * 对应是否开启 H2 控制台配置项的键
     */
    public static final String SPRING_H2_CONSOLE_ENABLED = "spring.h2.console.enabled";
    /**
     * 对应 H2 控制台路径配置项的键
     */
    public static final String SPRING_H2_CONSOLE_PATH = "spring.h2.console.path";
    public static final String SPRING_REGISTER_TO_NACOS = "spring.cloud.nacos.discovery.register-enabled";
    public static final String SPRING_REGISTER_AND_DISCOVERY_NACOS = "spring.cloud.nacos.discovery.enabled";


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

    public static final String NACOS_AUTH_GROUP = "easy4j-sauths";

    /**
     * http header
     */
    public static final String HTTP_HEADER_UNKNOWN = "unKnown";

    /**
     * http X-Forwarded-For
     */
    public static final String HTTP_X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * http X-Real-IP
     */
    public static final String HTTP_X_REAL_IP = "X-Real-IP";

    public static final String REDIS_CACHE_MANAGER = "redisCacheManager";
    public static final String CAFFEINE_CACHE_MANAGER = "caffeineCacheManager";
    public static final String REDIS_CONNECTION_FACTORY = "redissonConnectionFactory";


    public static final String SESSION_USER = "SESSION_USER";
}
