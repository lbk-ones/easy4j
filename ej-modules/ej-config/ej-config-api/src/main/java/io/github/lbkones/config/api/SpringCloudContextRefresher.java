package io.github.lbkones.config.api;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * 兼容springcloud 配置中心体系
 * @author bokun.li
 */
@Slf4j
public class SpringCloudContextRefresher {

    public static boolean hasSpringCloudEnvironment = false;


    private static Class<?> aClass;

    static {
        try {
            aClass = SpringCloudContextRefresher.class.getClassLoader().loadClass("org.springframework.cloud.context.refresh.ContextRefresher");
            hasSpringCloudEnvironment = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * 如果有springcloud环境那么直接刷新，否则返回false
     * @return boolean
     */
    public static boolean refresh(){
        if (hasSpringCloudEnvironment) {
            try {
                log.info("-------------begin refresh keys------------- ,{}",(aClass!=null?aClass.getName():""));
                Object bean = SpringUtil.getBean(aClass);
                Object refresh = ReflectUtil.invoke(bean, "refresh");
                log.info("-------------already refresh keys------------- {}", refresh);
            } catch (NoSuchBeanDefinitionException ignored) {
                return false;
            } catch (Exception ignored){
            }
            return true;
        }
        return false;

    }

}
