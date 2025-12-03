package easy4j.infra.rpc.integrated.spring.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.client.RpcProxyFactory;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.integrated.spring.SpringConnectionManager;
import easy4j.infra.rpc.integrated.spring.SpringE4jRpcConfig;
import easy4j.infra.rpc.integrated.spring.SpringServerInstanceInit;
import easy4j.infra.rpc.integrated.spring.annotations.RpcProxy;
import easy4j.infra.rpc.registry.Registry;
import easy4j.infra.rpc.registry.RegistryFactory;
import easy4j.infra.rpc.server.RpcServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * spring整合配置
 *
 * @author bokun
 * @since 2.0.1
 */
@Configuration
@EnableConfigurationProperties(value = {E4jRpcConfigSpring.class})
public class SpringConfig implements CommandLineRunner, BeanPostProcessor {
    Set<String> serverName = new HashSet<>();

    @Autowired
    E4jRpcConfigSpring springE4jRpcConfig;

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

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = ReflectUtil.getFields(bean.getClass());
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            if (field.isAnnotationPresent(RpcProxy.class)) {
                RpcProxy annotation = field.getAnnotation(RpcProxy.class);
                String value = annotation.value();
                if(StrUtil.isNotBlank(value)){
                    serverName.add(value);
                    Class<?> type = field.getType();
                    Object proxy = RpcProxyFactory.getProxy(type);
                    ReflectUtil.setFieldValue(bean,field,proxy);
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
            if(!serverName.isEmpty()){
                Registry registry = RegistryFactory.get();
                List<String> notFoundService = new ArrayList<>();
                for (String s : serverName) {
                    String s1 = RegisterInfoType.NODE.getRegisterPath() + StrPool.SLASH + s;
                    boolean exists = registry.exists(s1);
                    if(!exists){
                        notFoundService.add(s);
                    }
                }
                if(CollUtil.isNotEmpty(notFoundService)){
                    throw new RpcException("No providers were found for these services : "+ String.join("、", notFoundService));
                }
            }
        }
    }
}
