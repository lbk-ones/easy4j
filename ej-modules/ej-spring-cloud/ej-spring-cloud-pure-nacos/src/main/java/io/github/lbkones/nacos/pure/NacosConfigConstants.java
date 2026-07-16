package io.github.lbkones.nacos.pure;

/**
 * Nacos 配置常量类
 * 用于集中管理 Spring Cloud Nacos 的配置属性名称
 */
public class NacosConfigConstants {

    /**
     * Spring 应用基础配置
     */
    public static final String SPRING_APPLICATION_NAME = "spring.application.name";

    /**
     * Nacos 服务器配置
     */
    public static final String NACOS_SERVER_ADDR = "spring.cloud.nacos.server-addr";
    public static final String NACOS_USERNAME = "spring.cloud.nacos.username";
    public static final String NACOS_PASSWORD = "spring.cloud.nacos.password";
    public static final String CLOUD_REFRESHED = "spring.cloud.refresh.enabled";

    /**
     * Nacos 配置管理配置
     */
    public static final String NACOS_CONFIG_ENABLED = "spring.cloud.nacos.config.enabled";
    public static final String NACOS_CONFIG_FILE_EXTENSION = "spring.cloud.nacos.config.file-extension";
    public static final String NACOS_CONFIG_NAMESPACE = "spring.cloud.nacos.config.namespace";
    public static final String NACOS_CONFIG_GROUP = "spring.cloud.nacos.config.group";
    public static final String NACOS_CONFIG_USERNAME = "spring.cloud.nacos.config.username";
    public static final String NACOS_CONFIG_PASSWORD = "spring.cloud.nacos.config.password";
    public static final String NACOS_CONFIG_SERVER_ADDR = "spring.cloud.nacos.config.server-addr";
    public static final String SPRING_CONFIG_IMPORT = "spring.config.import";

    public static final String COMMA = ",";

    private NacosConfigConstants() {
        // 防止实例化
    }
}
