package io.github.lbkones.nacos.pure;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Set;

@Slf4j
public class CloudPropertiesRefresh implements ApplicationContextAware {
    private static boolean hasSpringCloudEnvironment = false;


    private static Class<?> aClass;

    static {
        try {
            aClass = CloudPropertiesRefresh.class.getClassLoader().loadClass("org.springframework.cloud.context.refresh.ContextRefresher");
            hasSpringCloudEnvironment = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void sendRefreshEvent(Set<String> keys) {

        if (keys == null || keys.isEmpty()) return;
        if (hasSpringCloudEnvironment) {
            try {
                log.info("-------------begin refresh keys------------- {},{}", keys,(aClass!=null?aClass.getName():""));
                Object bean = applicationContext.getBean(aClass);
                Object refresh = ReflectUtil.invoke(bean, "refresh");
                log.info("-------------already refresh keys------------- {}", refresh);
            } catch (Exception ignored) {
            }
        }
    }

}
