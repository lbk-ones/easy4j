package io.github.lbkones.nacos.pure;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class CloudPropertiesRefresh implements ApplicationContextAware {
    private static boolean hasSpringCloudEnvironment = false;

    private static volatile Constructor<?> constructor = null;

    static {
        try {
            CloudPropertiesRefresh.class.getClassLoader().loadClass("org.springframework.cloud.context.environment.EnvironmentChangeEvent");
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
            if (constructor == null) {
                synchronized (CloudPropertiesRefresh.class) {
                    if (constructor == null) {
                        try {
                            Class<?> aClass = Class.forName("org.springframework.cloud.context.environment.EnvironmentChangeEvent");
                            constructor = aClass.getConstructor(Set.class);
                        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                        }
                    }
                }
            }
            try {
                if (constructor == null) return;
                Object o = constructor.newInstance(keys);
                applicationContext.publishEvent(o);
            } catch (InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }

    }

}
