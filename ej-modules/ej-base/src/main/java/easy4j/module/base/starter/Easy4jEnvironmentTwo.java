package easy4j.module.base.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 环境初始化
 * @author bokun.li
 * @date 2023/10/30
 */
@Order(value = 13)
public class Easy4jEnvironmentTwo extends AbstractEnvironmentForEj {
    public static final String EASY4j_ENV_NAME = "easy4j-init-environment";

    @Override
    public String getName() {
        return EASY4j_ENV_NAME;
    }

    @Override
    public Properties getProperties() {
        return handlerDefaultAnnotationValues();
    }



    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        initEnv(environment,application);
        //String name = SystemUtil.getHostInfo().getName();
        //System.setProperty("LOG_FILE_NAME",this.getProperty("spring.application.name")+"-"+name.toLowerCase());
    }

}
