package easy4j.infra.es;

import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class EsEnv extends AbstractEasy4jEnvironment {
    public static final String NAME = "e4j-elasticsearch";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put("spring.data.elasticsearch.repositories.enabled","false");
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
