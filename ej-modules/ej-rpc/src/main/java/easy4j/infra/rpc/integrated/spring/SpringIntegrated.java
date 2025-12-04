package easy4j.infra.rpc.integrated.spring;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.client.GeneralizedInvoke;
import easy4j.infra.rpc.client.RpcClient;
import easy4j.infra.rpc.client.RpcClientFactory;
import easy4j.infra.rpc.client.RpcProxyFactory;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.integrated.spring.annotations.RpcProxy;
import easy4j.infra.rpc.integrated.spring.annotations.RpcService;
import easy4j.infra.rpc.registry.Registry;
import easy4j.infra.rpc.registry.RegistryFactory;
import easy4j.infra.rpc.server.RpcServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * spring整合配置
 *
 * @author bokun
 * @since 2.0.1
 */
@Configuration
@EnableConfigurationProperties(value = {E4jRpcConfigSpring.class})
public class SpringIntegrated implements ApplicationContextAware, CommandLineRunner, BeanPostProcessor, DisposableBean {
    Set<String> serverName = new HashSet<>();

    @Autowired
    E4jRpcConfigSpring springE4jRpcConfig;

    private ApplicationContext springContext;

    @Bean
    public SpringServerInstanceInit springServerInstanceInit() {
        return new SpringServerInstanceInit();
    }

    @Bean
    public SpringConnectionManager springConnectionManager() {
        return new SpringConnectionManager();
    }

    @Bean
    public SpringE4jRpcConfig springRpcConfig() {
        return new SpringE4jRpcConfig();
    }


    @Bean
    public GeneralizedInvoke generalizedInvoke(){
        return RpcProxyFactory.getGeneralizedProxy();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        springContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        String name = aClass.getName();
        if (BeanImport.getBasePackages().stream().anyMatch(name::startsWith)) {
            Field[] fields = ReflectUtil.getFields(aClass);
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isNative(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }
                if (field.isAnnotationPresent(RpcProxy.class)) {
                    RpcProxy annotation = field.getAnnotation(RpcProxy.class);
                    String value = annotation.value();
                    if (StrUtil.isNotBlank(value)) {
                        value = springContext.getEnvironment().resolvePlaceholders(value);
                        serverName.add(value);
                        Class<?> type = field.getType();
                        if (type.isInterface()) {
                            Object proxy = RpcProxyFactory.getProxy(type, value);
                            ReflectUtil.setFieldValue(bean, field, proxy);
                        }else{
                            throw new IllegalArgumentException("The field type of RpcProxy annotation must be a interface!");
                        }
                    } else {
                        Class<?> type = field.getType();
                        if (type.isInterface()) {
                            boolean annotationPresent = type.isAnnotationPresent(RpcService.class);
                            if (annotationPresent) {
                                String sn = type.getAnnotation(RpcService.class).serviceName();
                                if(StrUtil.isNotBlank(sn)){
                                    Object proxy = RpcProxyFactory.getProxy(type, sn);
                                    ReflectUtil.setFieldValue(bean, field, proxy);
                                }
                            }else{
                                throw new IllegalArgumentException("The value of RpcProxy annotation must be the service name!");
                            }
                        }else{
                            throw new IllegalArgumentException("The field type of RpcProxy annotation must be a interface!");
                        }
                    }
                }
            }
        }
        return bean;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!springE4jRpcConfig.getServer().isDisabled()) {
            ExecutorService executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("e4j-rpc-server-main-thread", true));
            executorService.execute(() -> {
                RpcServer rpcServer = new RpcServer(springE4jRpcConfig);
                rpcServer.start();
            });
        }

        if (springE4jRpcConfig.getClient().isCheck()) {
            if (!serverName.isEmpty()) {
                Registry registry = RegistryFactory.get();
                List<String> notFoundService = new ArrayList<>();
                for (String s : serverName) {
                    String s1 = RegisterInfoType.NODE.getRegisterPath() + StrPool.SLASH + s;
                    boolean exists = registry.exists(s1);
                    if (!exists) {
                        notFoundService.add(s);
                    }
                }
                if (CollUtil.isNotEmpty(notFoundService)) {
                    throw new RpcException("No providers were found for these services : " + String.join("、", notFoundService));
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        RpcClient client = RpcClientFactory.getClient();
        if (null != client) client.close();
    }
}
