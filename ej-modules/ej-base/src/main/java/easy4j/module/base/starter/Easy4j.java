package easy4j.module.base.starter;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import easy4j.module.base.enums.DbType;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.utils.SP;
import easy4j.module.base.utils.SqlType;
import easy4j.module.base.utils.SysConstant;
import jodd.util.PropertiesUtil;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.*;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 环境持有者
 *
 * @author bokun.li
 * @date 2023/10/30
 */
public class Easy4j implements ApplicationContextAware {

    public static ApplicationContext applicationContext;


    public static Environment environment;

    public static SpringApplication springApplication;

    public static DbType dbType;


    public static String mainClassPath = "";
    public static Class<?> mainClass;


    @Getter
    private static volatile Properties extProperties;

    @Getter
    private static final EjSysProperties defaultEjSysProperties = new EjSysProperties();


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Easy4j.applicationContext = applicationContext;
    }

    public static String getProperty(String name) {
        String property = environment.getProperty(name);
        if (StrUtil.isBlank(property)) {
            initExtProperties();
            return extProperties.getProperty(name);
        }
        return property;
    }


    private static String getDbUrlByEnvironment() {

        String property = getProperty(SysConstant.DB_URL_STR);
        if (StrUtil.isBlank(property)) {
            String url2 = getProperty(SysConstant.DB_URL_STR_NEW);
            if (StrUtil.isNotBlank(url2)) {
                String[] split = url2.split(SP.AT);
                property = split[0];
            }
        }
        return property;

    }


    private static String getDbTypeByEnvironment() {
        String property = getDbUrlByEnvironment();
        String dataTypeByUrl = SqlType.getDataTypeByUrl(property);
        DbType dbType = DbType.getDbType(dataTypeByUrl);
        return dbType.getDb();
    }


    public static String getDbUrl() {
        return getDbUrlByEnvironment();
    }

    public static String getDbType() {
        return getDbTypeByEnvironment();
    }

    // load ext properties
    // 1、load bootstrap class annotation metadata
    public static void initExtProperties() {
        if (extProperties == null) {
            synchronized (Easy4j.class) {
                if (extProperties == null) {
                    Map<String, Object> extMap = new HashMap<>();
                    Class<?> mainClass = Easy4j.mainClass;
                    if (Objects.nonNull(mainClass)) {
                        String serverName = null;
                        int serverPort = 0;
                        String ejDataSource = null;
                        boolean enableH2 = false;
                        String h2Url = "";
                        String h2ConsoleUsername = "sa";
                        String h2ConsolePassword = "password";
                        String author = "";
                        String serviceDesc = "";
                        Easy4JStarter annotation = mainClass.getAnnotation(Easy4JStarter.class);
                        Easy4JStarterNd annotation2 = mainClass.getAnnotation(Easy4JStarterNd.class);
                        Easy4JStarterTest annotation3 = mainClass.getAnnotation(Easy4JStarterTest.class);
                        if (Objects.nonNull(annotation)) {
                            serverName = annotation.serverName();
                            serverPort = annotation.serverPort();
                            ejDataSource = annotation.ejDataSourceUrl();
                            enableH2 = annotation.enableH2();
                            h2Url = annotation.h2Url();
                            author = annotation.author();
                            serviceDesc = annotation.serviceDesc();
                            h2ConsoleUsername = annotation.h2ConsoleUsername();
                            h2ConsolePassword = annotation.h2ConsolePassword();
                        } else if (Objects.nonNull(annotation2)) {
                            serverName = annotation2.serverName();
                            serverPort = annotation2.serverPort();
                            ejDataSource = annotation2.ejDataSourceUrl();
                            enableH2 = annotation2.enableH2();
                            h2Url = annotation2.h2Url();
                            author = annotation2.author();
                            serviceDesc = annotation2.serviceDesc();
                            h2ConsoleUsername = annotation2.h2ConsoleUsername();
                            h2ConsolePassword = annotation2.h2ConsolePassword();
                        } else if (Objects.nonNull(annotation3)) {
                            serverName = annotation3.serverName();
                            serverPort = annotation3.serverPort();
                            ejDataSource = annotation3.ejDataSourceUrl();
                            enableH2 = annotation3.enableH2();
                            h2Url = annotation3.h2Url();
                            author = annotation3.author();
                            serviceDesc = annotation3.serviceDesc();
                            h2ConsoleUsername = annotation3.h2ConsoleUsername();
                            h2ConsolePassword = annotation3.h2ConsolePassword();
                        }
                        if (StrUtil.isNotBlank(serverName)) {
                            extMap.put(SysConstant.SERVER_NAME, serverName);
                        }
                        extMap.put(SysConstant.H2_ENABLE, enableH2);
                        if (StrUtil.isNotBlank(h2Url)) {
                            extMap.put(SysConstant.H2_URL, h2Url);
                        }
                        if (StrUtil.isNotBlank(author)) {
                            extMap.put(SysConstant.AUTHOR, author);
                        }
                        if (StrUtil.isNotBlank(serviceDesc)) {
                            extMap.put(SysConstant.SERVICE_DESC, serviceDesc);
                        }
                        if (StrUtil.isNotBlank(h2ConsoleUsername)) {
                            extMap.put(SysConstant.H2_USER_NAME, h2ConsoleUsername);
                        }
                        if (StrUtil.isNotBlank(h2ConsolePassword)) {
                            extMap.put(SysConstant.H2_PASSWORD, h2ConsolePassword);
                        }
                        if (StrUtil.isNotBlank(ejDataSource)) {
                            extMap.put(SysConstant.DB_URL_STR_NEW, ejDataSource);
                        }
                        if (serverPort > 0) {
                            extMap.put(SysConstant.SERVER_PORT, serverPort);
                        }
                    } else {
                        System.err.println("not found mainClass...please check");
                    }
                    // append ext properties to spring env
                    MutablePropertySources propertySources = ((ConfigurableEnvironment) Easy4j.environment).getPropertySources();
                    MapPropertySource propertiesPropertySource = new MapPropertySource("easy4j-ext-properties", extMap);
                    propertySources.addLast(propertiesPropertySource);
                    extProperties = Convert.convert(Properties.class, extMap);
                }
            }
        }
    }

    /**
     * 获取系统参数
     *
     * @return
     */
    public static EjSysProperties getEjSysProperties() {
        Binder binder = Binder.get(environment);
        BindResult<EjSysProperties> easy4j = binder.bind(SysConstant.PARAM_PREFIX, EjSysProperties.class);
        try {
            return easy4j.get();
        } catch (Exception e) {
            return defaultEjSysProperties;
        }
    }


//    public static String jsonSerialization(Object object) {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//
//    }


}
