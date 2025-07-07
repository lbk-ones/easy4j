package easy4j.module.sca.config;

import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SP;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class SentinelEnvConfig extends AbstractEasy4jEnvironment {

    public static final String SENTINEL_ENV_NAME = "sca-sentinel-env-config";


    @Override
    public String getName() {
        return SENTINEL_ENV_NAME;
    }

    @Override
    public Properties getProperties() {
        /**
         feign.sentinel.enabled=true
         spring.cloud.sentinel.transport.port=8719
         spring.cloud.sentinel.transport.dashboard=localhost:8000
         spring.cloud.sentinel.log.dir=logs/sentinel
         spring.cloud.sentinel.eager=true
         */
        Properties properties = new Properties();
        boolean enableDashboard = getEnvProperty(Easy4j.getEjSysPropertyName(EjSysProperties::isSentinelDashboardEnable), boolean.class);
        if (enableDashboard) {
            int availablePort = findAvailablePort(8719);
            String dashboardUrl = getRequiredEnvProperty(Easy4j.getEjSysPropertyName(EjSysProperties::getSentinelDashboardUrl));
            properties.setProperty("spring.cloud.sentinel.transport.port", String.valueOf(availablePort));
            properties.setProperty("spring.cloud.sentinel.log.dir", getLogPath() + SP.SLASH + "csp");
            properties.setProperty("spring.cloud.sentinel.transport.dashboard", dashboardUrl);

            boolean isEager = getEnvProperty(Easy4j.getEjSysPropertyName(EjSysProperties::isSentinelDashboardEager), boolean.class);
            properties.setProperty("spring.cloud.sentinel.eager", String.valueOf(isEager));
            // 启用feign的熔断限流
            properties.setProperty("feign.sentinel.enabled", String.valueOf(true));
        }
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
