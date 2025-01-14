package easy4j.module.base.starter;

import ch.qos.logback.classic.PatternLayout;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import easy4j.module.base.utils.SysConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Objects;
import java.util.Properties;

/**
 * 环境初始化
 * @author bokun.li
 * @date 2023/10/30
 */
@Order(value = 13)
public class EnvironmentInitForEj extends AbstractEnvironmentForEj {
    public static final String EASY4j_ENV_NAME = "easy4j-init-environment";

    @Override
    public String getName() {
        return EASY4j_ENV_NAME;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();
        handlerDefaultAnnotationValues(properties);
        return properties;
    }

    private void handlerDefaultAnnotationValues(Properties properties) {
        Class<?> mainClass = EnvironmentHolder.mainClass;
        if(Objects.nonNull(mainClass)){
            Easy4JStarter annotation = mainClass.getAnnotation(Easy4JStarter.class);
            if(Objects.nonNull(annotation)){
                handlerDefaultValue(properties,  annotation.serverPort(), annotation.serverName());
            }
            Easy4JStarterNd annotation2 = mainClass.getAnnotation(Easy4JStarterNd.class);
            if(Objects.nonNull(annotation2)){
                handlerDefaultValue(properties, annotation2.serverPort(), annotation2.serverName());
            }
        }else{
            System.err.println("not found mainClass...please check");
        }
    }



    private void handlerDefaultValue(Properties properties, int serverPort, String serverName) {
        if(StrUtil.isNotBlank(serverName)){
            properties.setProperty(SysConstant.SERVER_NAME, serverName);
        }
        if(serverPort >0){
            properties.setProperty(SysConstant.SERVER_PORT,String.valueOf(serverPort));
        }
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        EnvironmentHolder.environment = environment;
        EnvironmentHolder.springApplication = application;
        String name = SystemUtil.getHostInfo().getName();
        System.setProperty("LOG_FILE_NAME",this.getProperty("spring.application.name")+"-"+name.toLowerCase());
    }

}
