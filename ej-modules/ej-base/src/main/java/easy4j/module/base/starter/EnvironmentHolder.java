package easy4j.module.base.starter;

import easy4j.module.base.enums.DbType;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
/**
 * 环境持有者
 * @author bokun.li
 * @date 2023/10/30
 */
public class EnvironmentHolder implements ApplicationContextAware {

    public static ApplicationContext applicationContext;


    public static Environment environment;

    public static SpringApplication springApplication;

    public static DbType dbType;


    public static String mainClassPath = "";
    public static Class<?> mainClass;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EnvironmentHolder.applicationContext = applicationContext;
    }

    public static String getProperty(String name){
        return environment.getProperty(name);
    }

}
