package easy4j.infra.rpc.integrated.spring;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.dialect.DialectFactory;
import easy4j.infra.rpc.client.GeneralizedInvoke;
import easy4j.infra.rpc.client.RpcClient;
import easy4j.infra.rpc.client.RpcClientFactory;
import easy4j.infra.rpc.client.RpcProxyFactory;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.enums.RegisterInfoType;
import easy4j.infra.rpc.enums.RegisterType;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.integrated.ServerInstanceInit;
import easy4j.infra.rpc.integrated.spring.annotations.RpcProxy;
import easy4j.infra.rpc.integrated.spring.annotations.RpcService;
import easy4j.infra.rpc.registry.Registry;
import easy4j.infra.rpc.registry.RegistryFactory;
import easy4j.infra.rpc.server.DefaultServerNode;
import easy4j.infra.rpc.server.RpcServer;
import easy4j.infra.rpc.utils.RpcJdbcTempDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
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
@Slf4j
public class SpringIntegrated implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware, BeanPostProcessor, DisposableBean {

    boolean isInit = false;

    private static volatile boolean isClear = false;

    Set<String> serverName = new HashSet<>();

    private ApplicationContext springContext;

    @Bean(name = "springServerInstanceInit")
    public ServerInstanceInit springServerInstanceInit() {
        return new SpringServerInstanceInit();
    }

    @Bean(name = "springConnectionManager")
    @ConditionalOnBean(value = DataSource.class)
    public SpringConnectionManager springConnectionManager() {
        return new SpringConnectionManager();
    }

    @Bean(name = "springRpcConfig")
    public SpringE4jRpcConfig springRpcConfig() {
        return new SpringE4jRpcConfig();
    }


    @Bean(name = "generalizedInvoke")
    public GeneralizedInvoke generalizedInvoke() {
        return RpcProxyFactory.getGeneralizedProxy();
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ApplicationContext applicationContext = event.getApplicationContext();

        if (!isInit && applicationContext.getParent() == null) {

            isInit = true;

            shutDownHook();

            registerIntegrated(applicationContext);

            E4jRpcConfig config = IntegratedFactory.getConfig();

            initRegistry(config);

            checkServer(config);

            startServer(config);

        }
    }

    private void shutDownHook() {
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::clear,"clear-shutdown-hook"));
    }

    /**
     * 获取数据库类型
     *
     * @param connection 数据库连接
     * @return 数据库类型
     */
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

    /**
     * 注册整合信息
     *
     * @param applicationContext spring上下文
     */
    private static void registerIntegrated(ApplicationContext applicationContext) {
        ListUtil.of("springConnectionManager", "springRpcConfig", "springServerInstanceInit")
                .forEach(e -> {
                    if (applicationContext.containsBean(e)) {
                        Object bean = applicationContext.getBean(e);
                        IntegratedFactory.register(bean);
                    }
                });
    }

    /**
     * 检查要代理的那些服务是否存在
     *
     * @param config 系统配置
     */
    private void checkServer(E4jRpcConfig config) {
        if (config.getClient().isCheck()) {
            if (!serverName.isEmpty()) {
                Registry registry = RegistryFactory.get();
                List<String> notFoundService = new ArrayList<>();
                for (String s : serverName) {
                    String s1 = RegisterInfoType.NODE.getRegisterPath() + StrPool.SLASH + s;
                    Collection<String> children = registry.children(s1);
                    if (children.isEmpty()) {
                        notFoundService.add(s);
                    }
                }
                if (CollUtil.isNotEmpty(notFoundService)) {
                    throw new RpcException("No providers were found for these services : " + String.join("、", notFoundService));
                }
            }
        }
    }

    /**
     * 开启服务
     *
     * @param config
     */
    private static void startServer(E4jRpcConfig config) {
        if (!config.getServer().isDisabled()) {
            ExecutorService executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("e4j-rpc-server-main-thread", true));
            executorService.execute(() -> {
                RpcServer rpcServer = new RpcServer(config);
                rpcServer.start();
            });
        }
    }

    /**
     * 初始化jdbc当注册中心
     *
     * @param config 系统配置
     */
    private void initRegistry(E4jRpcConfig config) {
        RegistryFactory.get().start();
        if (config.getRegisterType() == RegisterType.JDBC) {
            String registryJdbcUrl = config.getRegistryJdbcUrl();
            String registryJdbcUsername = config.getRegistryJdbcUsername();
            String registryJdbcPassword = config.getRegistryJdbcPassword();
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
                log.info("not found easy4j rpc registryJdbcUrl,registryJdbcUsername,registryJdbcPassword ,can not instance sql script!");
            }
        }
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
                    Class<?> type = field.getType();
                    if (!type.isInterface()) {
                        throw new IllegalArgumentException("The field type of RpcProxy annotation must be a interface!");
                    }
                    if (StrUtil.isNotBlank(value)) {
                        value = springContext.getEnvironment().resolvePlaceholders(value);
                        serverName.add(value);
                        FilterAttributes filterAttributes = new FilterAttributes()
                                .setServiceName(value)
                                .setBroadcast(annotation.broadcast())
                                .setBroadcastAsync(annotation.broadcastAsync())
                                .setUrl(annotation.url())
                                .setInvokeRetryMaxCount(annotation.invokeRetryMaxCount())
                                .setTimeOut(annotation.timeOut());
                        Object proxy = RpcProxyFactory.getProxy(type, filterAttributes);
                        ReflectUtil.setFieldValue(bean, field, proxy);
                    } else {
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
                    }
                }
            }
        }
        return bean;
    }

    @Override
    public void destroy() throws Exception {
        clear();
    }

    public synchronized void clear(){
        if(!isClear){
            isClear = true;
            RpcClient client = RpcClientFactory.getClient();
            if (null != client) client.close();
            try {
                RegistryFactory.get().close();
            } catch (IOException ignored) {
            }
            DefaultServerNode.INSTANCE.unRegistry();
        }

    }
}
