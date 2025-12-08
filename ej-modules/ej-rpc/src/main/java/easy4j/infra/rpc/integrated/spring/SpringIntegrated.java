package easy4j.infra.rpc.integrated.spring;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.dialect.DialectFactory;
import easy4j.infra.rpc.client.GeneralizedInvoke;
import easy4j.infra.rpc.client.RpcClient;
import easy4j.infra.rpc.client.RpcClientFactory;
import easy4j.infra.rpc.client.RpcProxyFactory;
import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.integrated.ServerInstanceInit;
import easy4j.infra.rpc.integrated.spring.annotations.RpcProxy;
import easy4j.infra.rpc.integrated.spring.annotations.RpcService;
import easy4j.infra.rpc.registry.Registry;
import easy4j.infra.rpc.registry.RegistryFactory;
import easy4j.infra.rpc.server.RpcServer;
import easy4j.infra.rpc.utils.RpcJdbcTempDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
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
    public ServerInstanceInit springServerInstanceInit() {
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
    public GeneralizedInvoke generalizedInvoke() {
        return RpcProxyFactory.getGeneralizedProxy();
    }

    @Bean
    public Object register(ServerInstanceInit serverInstanceInit, SpringConnectionManager springConnectionManager, SpringE4jRpcConfig springRpcConfig) {
        IntegratedFactory.register(serverInstanceInit);
        IntegratedFactory.register(springConnectionManager);
        IntegratedFactory.register(springRpcConfig);
        return null;
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
                            FilterAttributes filterAttributes = new FilterAttributes()
                                    .setServiceName(value)
                                    .setTimeOut(annotation.timeOut());
                            Object proxy = RpcProxyFactory.getProxy(type, filterAttributes);
                            ReflectUtil.setFieldValue(bean, field, proxy);
                        } else {
                            throw new IllegalArgumentException("The field type of RpcProxy annotation must be a interface!");
                        }
                    } else {
                        Class<?> type = field.getType();
                        if (type.isInterface()) {
                            boolean annotationPresent = type.isAnnotationPresent(RpcService.class);
                            if (annotationPresent) {
                                String sn = type.getAnnotation(RpcService.class).serviceName();
                                if (StrUtil.isNotBlank(sn)) {
                                    FilterAttributes filterAttributes = new FilterAttributes()
                                            .setServiceName(sn)
                                            .setTimeOut(annotation.timeOut());
                                    Object proxy = RpcProxyFactory.getProxy(type, filterAttributes);
                                    ReflectUtil.setFieldValue(bean, field, proxy);
                                }
                            } else {
                                throw new IllegalArgumentException("The value of RpcProxy annotation must be the service name!");
                            }
                        } else {
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


    @ConditionalOnProperty(
            prefix = "easy4j.rpc",
            name = {"register-type"},
            havingValue = "jdbc"
    )
    @Configuration(proxyBeanMethods = false)
    @Slf4j
    public static class JdbcRegistry implements InitializingBean {

        @Autowired
        E4jRpcConfigSpring e4jRpcConfigSpring;


        public String getDbType(Connection connection) {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String databaseProductName = metaData.getDatabaseProductName();
                return switch (databaseProductName) {
                    case "H2" -> "h2";
                    case "MySQL" -> "mysql";
                    case "Oracle" -> "oracle";
                    case "PostgreSQL" -> "postgresql";
                    case "Microsoft SQL Server" -> "sqlserver";
                    default -> {
                        if (databaseProductName.startsWith("DB2")) {
                            yield "db2";
                        }
                        yield null;
                    }
                };
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void afterPropertiesSet() throws Exception {
            String registryJdbcUrl = e4jRpcConfigSpring.getRegistryJdbcUrl();
            String registryJdbcUsername = e4jRpcConfigSpring.getRegistryJdbcUsername();
            String registryJdbcPassword = e4jRpcConfigSpring.getRegistryJdbcPassword();
            if (StrUtil.isAllNotBlank(registryJdbcUrl, registryJdbcUsername, registryJdbcPassword)) {
                String s = DialectFactory.identifyDriver(registryJdbcUrl);
                RpcJdbcTempDataSource rpcJdbcTempDataSource = new RpcJdbcTempDataSource(s, registryJdbcUrl, registryJdbcUsername, registryJdbcPassword);
                try (Connection connection = rpcJdbcTempDataSource.getConnection()) {
                    String sqlType = getDbType(connection);
                    ClassPathResource classPathResource = new ClassPathResource("db/registry/" + sqlType + ".sql");
                    if (classPathResource.exists()) {
                        StringBuilder sb = new StringBuilder();
                        try (InputStreamReader inputStream = new InputStreamReader(classPathResource.getInputStream()); BufferedReader bufferedReader = new BufferedReader(inputStream)) {
                            char[] buffer = new char[8192];
                            int length;
                            while ((length = bufferedReader.read(buffer)) != -1) {
                                sb.append(buffer, 0, length);
                            }
                        }
                        if (!sb.isEmpty()) {
                            DDlExe.execDDL(connection, sb.toString(), null, false);
                            log.info("easy4j rpc sql script successful instanced");
                        }
                    }
                } catch (Exception e) {
                    log.info("easy4j rpc sql script has been instanced");
                }
            } else {
                log.warn("not found easy4j rpc registryJdbcUrl,registryJdbcUsername,registryJdbcPassword ,can not instance sql script!");
            }
        }
    }
}
