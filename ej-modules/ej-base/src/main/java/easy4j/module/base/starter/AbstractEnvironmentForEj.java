package easy4j.module.base.starter;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.log.DefLog;
import easy4j.module.base.utils.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
/**
 * 抽象处理配置
 * 继承这个之后拿取配置一定能拿到除了远程配置中心加载之外的所有配置
 * @author bokun.li
 * @date 2023/10/30
 */
public abstract class AbstractEnvironmentForEj implements EnvironmentPostProcessor {


//    private static final DeferredLog LOGGER = new DeferredLog();
//
//    @Override
//    public void onApplicationEvent(ApplicationEvent event) {
//        LOGGER.replayTo(AbstractEnvironmentForEj.class);
//    }

    public abstract String getName();

    public abstract Properties getProperties();

    public DefLog getLogger(){
        return ObjectHolder.INSTANCE.getOrNewObject(DefLog.class,new DefLog());
    }

    public abstract void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application);

    // 环境初始化
    public void initEnv(ConfigurableEnvironment environment,SpringApplication application){
        if(Easy4j.environment == null){
            Easy4j.environment = environment;
        }
        if (Easy4j.springApplication == null) {
            Easy4j.springApplication = application;
        }
    }

    @Override
    public final void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        initEnv(environment,application);
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
        return Easy4j.getDbType();
    }

    public String getDbUrl(){
        return Easy4j.getDbUrl();
    }


    public String getProperty(String name){
        return Easy4j.getProperty(name);
    }

    public Properties handlerDefaultAnnotationValues() {
        // shallow copy
        return new Properties(Easy4j.getExtProperties());
    }
}
