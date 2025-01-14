package easy4j.module.mybatisplus;

import easy4j.module.base.starter.AbstractEnvironmentForEj;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;
/**
 * 参数覆盖
 * @author bokun.li
 * @date 2023/11/18
 */
public class MybatisPlusEnvironmentProperties extends AbstractEnvironmentForEj {

    public static final String P_NAME = "easy4j-mybatis-plus";

    @Override
    public String getName() {
        return P_NAME;
    }

    /**
     * mapperLocations
     * @return
     */
    public Properties getProperties(){
        // 关闭一级缓存 和二级缓存 （单服务可以不用关闭一级缓存）
        Properties properties = new Properties();
        properties.setProperty("mybatis-plus.configuration.cache-enabled","false");
        properties.setProperty("mybatis-plus.configuration.localCacheScope","STATEMENT");
        properties.setProperty("mybatis-plus.global-config.banner","false");
        return properties;
    }


    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
