package easy4j.module.base.starter;

import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.utils.SysConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 先于spring配置加载 所以这个时候配置的手动拿取
 */
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER - 1)
public class Easy4jEnvironmentFirst extends AbstractEnvironmentForEj {

    public static final String SCA_PROPERTIES_NAME = "easy4j-environment-first";

    @Override
    public String getName() {
        return SCA_PROPERTIES_NAME;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        preLoadApplicationProfile();
    }
}
