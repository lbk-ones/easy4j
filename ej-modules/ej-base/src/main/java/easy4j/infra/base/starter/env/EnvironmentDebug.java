package easy4j.infra.base.starter.env;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.config.StringConfigToPropertySourceUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Order(value = ConfigDataEnvironmentPostProcessor.ORDER + 1000)
public class EnvironmentDebug extends AbstractEasy4jEnvironment{

    private static  boolean EXED = false;

    @Override
    public String getName() {
        return "environment-debug";
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        if(!EXED){
            try{
                String initParameterValue = getInitParameterValue("EASY4J_ENV_DEBUG");
                if(StrUtil.equals("true",initParameterValue)){
                    MutablePropertySources propertySources = environment.getPropertySources();
                    for (PropertySource<?> next : propertySources) {
                        String name = next.getName();
                        System.out.println(" ");
                        System.out.println(SysLog.compact("  =============⬇⬇⬇⬇⬇⬇⬇===============>  " + name));
                        System.out.println(" ");
                        Map<String, Object> map = StringConfigToPropertySourceUtils.toMap(next);
                        Set<Map.Entry<String, Object>> entries = map.entrySet();
                        for (Map.Entry<String, Object> entry : entries) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            System.out.println(SysLog.compact(key + " = " + value));
                        }
                    }
                }
            }catch (Exception ignored){
            }

            EXED = true;
        }

    }
}
