/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.base.starter.env;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.base.starter.Easy4JStarterNd;
import easy4j.infra.base.starter.Easy4JStarterTest;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.Easy4jContext;
import jodd.util.StringPool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 环境持有者
 *
 * @author bokun.li
 * @date 2023/10/30
 */
@Slf4j
public class Easy4j implements ApplicationContextAware {

    public static final String EJ_SYS_ANNOTATION_PROPERTIES = "ej-sys-annotation-properties";

    public static ApplicationContext applicationContext;


    public static Environment environment;

    public static SpringApplication springApplication;

    public static DbType dbType;


    public static String mainClassPath = "";
    public static Class<?> mainClass;
    private static final AtomicBoolean isReady = new AtomicBoolean(false);

    public static boolean isReady() {
        return isReady.get();
    }

    public static void ready() {
        if (isReady.get()) {
            return;
        }
        isReady.compareAndSet(false, true);
    }

    public final static Map<String, AtomicBoolean> isInitPreLoadApplication = Maps.newHashMap();


    @Getter
    private static volatile Properties extProperties;

    @Getter
    private static final EjSysProperties defaultEjSysProperties = new EjSysProperties();
    private static final Map<String, Object> defaultMap = new HashMap<>();


    // 提取初始值 不想去用反射拿影响性能
    static {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(defaultEjSysProperties, true, false);
        for (String key : stringObjectMap.keySet()) {
            Object o = stringObjectMap.get(key);
            String s = SysConstant.PARAM_PREFIX + StringPool.DOT + key.replaceAll(StringPool.UNDERSCORE, StringPool.DASH);
            defaultMap.put(s, o);
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Easy4j.applicationContext = applicationContext;
    }

    public static String getProperty(String name) {
        return getProperty(name, String.class);
    }

    public static <T> T getRequiredProperty(String name, Class<T> aclass) {
        return getPropertyWith(name, aclass, true, null);
    }

    public static String getRequiredProperty(String name) {
        return getPropertyWith(name, String.class, true, null);
    }

    public static <T> T getProperty(String name, Class<T> aclass) {
        return getPropertyWith(name, aclass, false, null);
    }

    public static <T> T getEnvProperty(String name, Class<T> aclass, Environment environment) {
        return getPropertyWith(name, aclass, false, environment);
    }

    public static <T> T getRequiredEnvProperty(String name, Class<T> aclass, Environment environment) {
        return getPropertyWith(name, aclass, true, environment);
    }

    private static <T> T getPropertyWith(String name, Class<T> aclass, boolean isRequired, Environment _environment) {
        boolean isNullEnv = _environment == null;
        Environment final_environment = _environment;
        if (isNullEnv) _environment = environment;
        T property = Optional.ofNullable(_environment)
                .map(e -> e.getProperty(name, aclass))
                .orElse(null);
        if (ObjectUtil.isEmpty(property)) {
            initExtProperties();
            property = aclass.cast(extProperties.getProperty(name));
        }
        boolean isSys = name.startsWith(SysConstant.PARAM_PREFIX + StringPool.DOT);
        String[] staticVs = null;
        if (isSys) {
            if (ObjectUtil.isEmpty(property)) {
                staticVs = EjSysProperties.getStaticVs(name);

                property = Optional.ofNullable(staticVs)
                        .map(e -> {
                            String s = e[0];
                            if (!StrUtil.equalsAnyIgnoreCase(s, name)) {
                                if (isNullEnv) {
                                    return getProperty(s, aclass);
                                } else {
                                    return getEnvProperty(s, aclass, final_environment);
                                }
                            } else {
                                return null;
                            }
                        })
                        .orElse(null);
            }
            if (ObjectUtil.isEmpty(property)) {
                Object orDefault = defaultMap.get(name);
                if (!ObjectUtil.isEmpty(orDefault)) {
                    return Convert.convert(aclass, orDefault);
                }
            }
        }
        if (isRequired && ObjectUtil.isEmpty(property)) {
            errorInfo(name, staticVs);
        }
        return property;
    }

    private static void errorInfo(String name, String[] staticVs) {
        List<String> objects = ListTs.newArrayList();
        objects.add(name);
        if (staticVs != null) {
            for (String staticV : staticVs) {
                if (StrUtil.isNotBlank(staticV)) {
                    objects.add(staticV);
                }
            }
        }
        String join = String.join("或", objects);
        throw new EasyException("请设置参数：" + join);
    }

    /**
     * 先从给定的map拿、如果没有再走getProperty接下来的流程
     *
     * @param init
     * @param name
     * @param aclass
     * @param <T>
     * @return
     */
    public static <T> T getProperty(Map<String, Object> init, String name, Class<T> aclass) {
        Object o = Optional.ofNullable(init)
                .map(e -> e.get(name))
                .orElse(null);
        if (Objects.nonNull(o)) {
            return Convert.convert(aclass, o);
        }
        return getProperty(name, aclass);
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
    // load bootstrap class annotation metadata
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
                            extMap.put(SysConstant.SPRING_SERVER_NAME, serverName);
                        }
                        extMap.put(SysConstant.H2_ENABLE, enableH2);
                        if (StrUtil.isNotBlank(h2Url)) {
                            extMap.put(SysConstant.H2_URL, h2Url);
                        }
                        if (StrUtil.isNotBlank(author)) {
                            extMap.put(SysConstant.AUTHOR, author);
                        }
                        if (StrUtil.isNotBlank(serviceDesc)) {
                            extMap.put(SysConstant.EASY4J_SERVICE_DESC, serviceDesc);
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
                            extMap.put(SysConstant.SPRING_SERVER_PORT, serverPort);
                        }
                    } else {
                        System.err.println("not found mainClass...please check");
                    }
                    // append ext properties to spring env
                    MutablePropertySources propertySources = ((ConfigurableEnvironment) Easy4j.environment).getPropertySources();
                    MapPropertySource propertiesPropertySource = new MapPropertySource(EJ_SYS_ANNOTATION_PROPERTIES, extMap);
                    propertySources.addLast(propertiesPropertySource);
                    extProperties = Convert.convert(Properties.class, extMap);
                }
            }
        }
    }

    /**
     * 获取系统参数 系统启动阶段使用 最好不在下游服务调用 他是有一定消耗的
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

    public static String getEjSysPropertyName(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers)) {
            return null;
        }
        String name = field.getName();
        String underlineCase = StrUtil.toUnderlineCase(name);
        String lowerCase = underlineCase.toLowerCase();
        String s = lowerCase.replaceAll(StringPool.UNDERSCORE, StringPool.DASH);
        return SysConstant.PARAM_PREFIX + StringPool.DOT + s;
    }

    public static <T> String getEjSysPropertyName(Func1<T, ?> func) {
        Field field = ReflectUtil.getField(EjSysProperties.class, LambdaUtil.getFieldName(func));
        return getEjSysPropertyName(field);
    }

    private static final class Easy4jContextHolder {
        static final Easy4jContext easy4jContext = SpringUtil.getBean(Easy4jContext.class);
    }

    public static Easy4jContext getContext() {
        return Easy4jContextHolder.easy4jContext;
    }


    public static void info(String msg, Object... objects) {
        log.info(msg, objects);
    }

    public static void error(String msg, Object... objects) {
        log.error(msg, objects);
    }

    public static void error(String msg, Throwable objects) {
        log.error(msg, objects);
    }

    public static void debug(String msg, Object... objects) {
        log.debug(msg, objects);
    }

    public static void warn(String msg, Object... objects) {
        log.warn(msg, objects);
    }


    // 程序运行时拿取
    public static String getType() {
        DbType dbType = getDbType1();
        return dbType.getDb();
    }

    private static DbType getDbType1() {
        String url = Easy4j.environment.getRequiredProperty(SysConstant.DB_URL_STR);

        if (StrUtil.isBlank(url)) {
            String url2 = Easy4j.environment.getProperty(SysConstant.DB_URL_STR_NEW);
            if (StrUtil.isNotBlank(url2)) {
                String[] split = url2.split("@");
                url = split[0];
            }
        }

        String dataTypeByUrl = SqlType.getDataTypeByUrl(url);
        return DbType.getDbType(dataTypeByUrl);
    }


}
