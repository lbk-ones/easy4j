package easy4j.modules.ltl.transactional;

import easy4j.module.base.starter.AbstractEnvironmentForEj;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class TransactionEnvironmentForEj extends AbstractEnvironmentForEj {
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


