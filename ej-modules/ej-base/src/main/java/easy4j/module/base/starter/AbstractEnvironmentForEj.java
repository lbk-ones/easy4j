package easy4j.module.base.starter;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.enums.DbType;
import easy4j.module.base.log.DefLog;
import easy4j.module.base.utils.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
/**
 * 抽象处理配置
 * @author bokun.li
 * @date 2023/10/30
 */
public abstract class AbstractEnvironmentForEj implements EnvironmentPostProcessor {


    private ConfigurableEnvironment environment;

//    private static final DeferredLog LOGGER = new DeferredLog();
//
//    @Override
//    public void onApplicationEvent(ApplicationEvent event) {
//        LOGGER.replayTo(AbstractEnvironmentForEj.class);
//    }

    public abstract String getName();

    public abstract Properties getProperties();

    public ConfigurableEnvironment getEnvironment(){
        return this.environment;
    }
    public DefLog getLogger(){
        return ObjectHolder.INSTANCE.getOrNewObject(DefLog.class,new DefLog());
    }

    public abstract void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application);

    @Override
    public final void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        this.environment = environment;
        MutablePropertySources propertySources = environment.getPropertySources();
        Properties properties = getProperties();
        String name = getName();
        if (StrUtil.isNotBlank(name)) {
            if(name.endsWith(".yml") || name.endsWith(".yaml")){
                YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
                Resource resource = new ClassPathResource(name);
                if (resource.exists()) {
                    try {
                        PropertySource<?> propertySource = loader.load(name, resource).get(0);
                        environment.getPropertySources().addLast(propertySource);
                        getLogger().info(SysLog.compact(name.toLowerCase()+"参数完成覆盖"));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load custom YAML configuration", e);
                    }
                }
            }else{
                if(Objects.nonNull(properties)){
                    PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(name,properties);
                    propertySources.addLast(propertiesPropertySource);
                    getLogger().info(SysLog.compact(name.toLowerCase()+"参数完成覆盖"));
                }
            }
        }

        handlerEnvironMent(environment,application);
    }

    public String getDbType(){
        return EnvironmentHolder.getDbType(this.environment);
    }

    public String getDbUrl(){
        return EnvironmentHolder.getDbUrl(this.environment);
    }


    public String getProperty(String name){
        return this.environment.getProperty(name);
    }

}
