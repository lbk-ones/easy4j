package easy4j.module.idempotent;

import easy4j.module.base.starter.AbstractEnvironmentForEj;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * IdempotentEnvironment
 *
 * @author bokun.li
 * @date 2025-05
 */
public class IdempotentEnvironment extends AbstractEnvironmentForEj {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}