package easy4j.infra.rpc.integrated.spring;

import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.integrated.config.AbstractRpcConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

/**
 * 整合spring的配置
 *
 * @since 2.0.1
 */
public class SpringE4jRpcConfig extends AbstractRpcConfig implements ApplicationContextAware, BeanNameAware {

    String beanName;

    ApplicationContext context;

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        Object bean = context.getBean(beanName);
        IntegratedFactory.register(bean);
    }

    @Override
    public E4jRpcConfig getConfig() {
        Environment environment = context.getEnvironment();
        Binder binder = Binder.get(environment);
        BindResult<E4jRpcConfig> bind = binder.bind("easy4j.rpc", E4jRpcConfig.class);
        return bind.get();
    }

    @Override
    public String defaultGet(String key) {
        return context.getEnvironment().getProperty(key);
    }
}
