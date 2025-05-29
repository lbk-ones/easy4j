package easy4j.module.dubbo3;

import easy4j.module.base.starter.AbstractEnvironmentForEj;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * Dubbo3Environment
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Dubbo3Environment extends AbstractEnvironmentForEj {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Properties getProperties() {

        Properties properties = new Properties();
        properties.setProperty("","");
        properties.setProperty("","");
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}