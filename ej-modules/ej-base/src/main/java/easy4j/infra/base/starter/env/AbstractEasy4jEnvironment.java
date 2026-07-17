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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysFieldInfo;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.resolve.AbstractEasy4jResolve;
import easy4j.infra.base.resolve.BootStrapSpecialVsResolve;
import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.*;
import jodd.util.StringPool;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽象处理配置
 * 继承这个之后拿取配置一定能拿到除了远程配置中心加载之外的所有配置
 *
 * @author bokun.li
 * @date 2023/10/30
 */
@Getter
public abstract class AbstractEasy4jEnvironment extends StandAbstractEasy4jResolve implements EnvironmentPostProcessor {


    //    private static final DeferredLog LOGGER = new DeferredLog();
//
//    @Override
//    public void onApplicationEvent(ApplicationEvent event) {
//        LOGGER.replayTo(AbstractEnvironmentForEj.class);
//    }
    public ConfigurableEnvironment configEnvironment;

    public static final String FIRST_ENV_NAME = "first-load-env-file-for-" + SysConstant.PARAM_PREFIX;

    public abstract String getName();

    public abstract Properties getProperties();

    public DefLog getLogger() {
        return ObjectHolder.INSTANCE.getOrNewObject(DefLog.class, new DefLog());
    }

    public abstract void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application);

    // 环境初始化
    public void initEnv(ConfigurableEnvironment environment, SpringApplication application) {
        if (Easy4j.environment == null) {
            Easy4j.environment = environment;
        }
        if (Easy4j.springApplication == null) {
            Easy4j.springApplication = application;
        }
        if (null == configEnvironment) {
            configEnvironment = environment;
        }
    }

    /**
     * 根据name兼容获取资源
     *
     * @author bokun.li
     * @date 2025/6/24
     */
    public Resource getResourceFromName(String name) {
        try {
            File file = new File(name);
            if (file.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    return new InputStreamResource(fileInputStream);
                } catch (FileNotFoundException ignored) {
                }
            } else {
                ClassPathResource classPathResource = new ClassPathResource(name);
                if (classPathResource.exists()) {
                    return classPathResource;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 是否跳过环境设置
     * 由子类重写
     *
     * @author bokun.li
     * @date 2025/6/24
     */
    public boolean isSkip() {
        return false;
    }

    /**
     * custom properties and file together load
     *
     * @author bokun.li
     * @date 2025/6/24
     */
    @Override
    public final void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            String name = getName();
            initEnv(environment, application);
            if (isSkip()) {
                System.out.println(SysLog.compact("skip " + getName() + " config.."));
                return;
            }
            MutablePropertySources propertySources = environment.getPropertySources();
            Properties properties = getProperties();
            if (StrUtil.isNotBlank(name)) {
                boolean isPost = false;
                PropertySourceLoader loader = null;
                if (name.endsWith(SP.YML_SUFFIX) || name.endsWith(SP.YAML_SUFFIX)) {
                    loader = new YamlPropertySourceLoader();
                } else if (name.endsWith(SP.PROPERTIES_SUFFIX)) {
                    if (name.startsWith(SP.APPLICATION) || name.startsWith(SP.BOOTSTRAP)) {
                        throw new EasyException("name can not start with " + SP.APPLICATION + " , " + SP.BOOTSTRAP);
                    }
                    loader = new PropertiesPropertySourceLoader();
                }
                Resource resource = getResourceFromName(name);
                if (null != resource && resource.exists() && null != loader) {
                    try {
                        List<PropertySource<?>> load = loader.load(name, resource);
                        PropertySource<?> propertySource = null;
                        if (load.size() == 1) {
                            propertySource = load.get(0);
                        } else if (load.size() > 1) {
                            CompositePropertySource compositePropertySource = new CompositePropertySource(name);
                            for (PropertySource<?> source : load) {
                                compositePropertySource.addPropertySource(source);
                            }
                            propertySource = compositePropertySource;
                        }
                        if (propertySource != null) {
                            environment.getPropertySources().addLast(propertySource);
                            getLogger().info(SysLog.compact("the " + name.toLowerCase() + " parameter is SuccessFull replaced。"));
                            isPost = true;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load custom YAML configuration", e);
                    }
                }
                if (Objects.nonNull(properties) && !properties.isEmpty()) {
                    String name2 = name;
                    if (isPost) {
                        // override file properties
                        name2 += "--";
                        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(name2, properties);
                        propertySources.addBefore(name, propertiesPropertySource);
                        getLogger().info(SysLog.compact("the " + name2.toLowerCase() + " parameter is SuccessFull replaced。"));
                    } else {
                        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(name2, properties);
                        propertySources.addLast(propertiesPropertySource);
                        getLogger().info(SysLog.compact("the " + name2.toLowerCase() + " parameter is SuccessFull replaced。"));
                    }

                }
            }
            handlerEnvironMent(environment, application);
        } catch (Throwable e) {
            if (!Easy4j.isReady()) {
                throw e;
            } else {
                Easy4j.error("An Runtime error occurred in Environment ：", e);
            }
        }

    }

    public Properties handlerDefaultAnnotationValues() {
        Easy4j.initExtProperties();
        // shallow copy
        return new Properties(Easy4j.getExtProperties());
    }

    public Set<String> getProfileNameList(PropertySource<?> propertySource) {

        Set<String> list = new HashSet<>();
        if (propertySource == null) {
            // 处理 spring.profiles.active
            String property = getInitParameterValue(SysConstant.SPRING_PROFILE_ACTIVE);
            if (StrUtil.isNotBlank(property)) {
                list.add(property);
            }
            // 处理 spring.profiles.include
            String property2 = getInitParameterValue(SysConstant.SPRING_PROFILE_INCLUDES);
            if (StrUtil.isNotBlank(property2)) {
                list.add(property2);
            }
        } else {
            String property = Convert.toStr(propertySource.getProperty(SysConstant.SPRING_PROFILE_ACTIVE));
            if (StrUtil.isNotBlank(property)) {
                String[] split = property.split(SP.COMMA);
                list.addAll(Arrays.asList(split));
            }
            String include = Convert.toStr(propertySource.getProperty(SysConstant.SPRING_PROFILE_INCLUDES));
            if (StrUtil.isNotBlank(include)) {
                String[] split = include.split(SP.COMMA);
                list.addAll(Arrays.asList(split));
            }
        }
        return list;
    }

    /**
     * 从 命令行参数、-D、env  中拿取
     * 优先级 命令行参数  >  -D > env
     *
     * @param name 参数名称
     */
    public String getInitParameterValue(String name) {
        Map<String, String> argsMap = Easy4j.getSpringInputArgsMap();
        String var1 = argsMap.get(name);
        String var2 = System.getProperty(name);
        String var3 = System.getenv(name);
        return StrUtil.firstNonBlank(var1, var2, var3);
    }

    /**
     * 不借助springboot提前加载配置文件
     * 这个时候springboot 参数还没开始初始化 在这里一股脑把所有easy4j 开头的参数全部扔进去 且优先级最高
     * 参数优先级排列如下:
     * 命令行参数 > -D系统参数 > 环境变量 > 指定配置文件参数（config.location,addition-location,config.import） > 默认配置文件参数(jar包所在同级文件，同级config下面的文件) > 文件参数(application.properties/yml/yaml) > annotation > remote config
     * 其实这样目前有个弊端那就是 这些参数的优先级比所有配置优先级都要高了 哪怕nacos/apollo配置中心配置了相关参数也不会生效
     * 有个解决方法（nacos更新的时候监控更新数据然后动态替换这个最高级优先配置）
     * ps:这里只处理easy4j.开头的系统参数
     */
    public void preLoadApplicationProfile() {

        MutablePropertySources propertySources = configEnvironment.getPropertySources();
        PropertySource<?> propertySource = propertySources.get(FIRST_ENV_NAME);
        if (ObjectUtil.isNotEmpty(propertySource)) {
            return;
        }
        System.out.println(SysLog.compact("begin early load springboot config file...."));

        List<PropertySource<?>> allProperties = ListTs.newLinkedList();

        // handler spring.config.location and spring.config.additional-location and spring.config.import，if theme has values
        String configLocation = getInitParameterValue(SysConstant.SPRING_CONFIG_LOCATION);
        String additionalLocation = getInitParameterValue(SysConstant.SPRING_CONFIG_ADDITIONAL_LOCATION);
        String configImport = getInitParameterValue(SysConstant.SPRING_CONFIG_IMPORT);
        SpringBootConfigLoader sbcl = new SpringBootConfigLoader();
        sbcl.loadConfigs(configLocation, additionalLocation, configImport);
        sbcl.printLoadSummary();
        LinkedList<PropertySource<?>> locationProperties = sbcl.getPropertyResolvers();
        ListTs.addAll(allProperties, locationProperties);

        // get from jvm env
        Set<String> envProfileActive = getProfileNameList(null);
        System.out.println(SysLog.compact("env profile active " + String.join(SP.COMMA, envProfileActive)));

        String fileNameInit = "application";
        String originFileNameInit = fileNameInit;
        Set<String> defaultProfileNameList = new HashSet<>();
        // load default properties
        PropertySource<?> defaultProperties = loadPropertySource(fileNameInit);
        if (defaultProperties != null) {
            System.out.println(SysLog.compact("find config file：" + defaultProperties.getName()));
            allProperties.add(defaultProperties);
            defaultProfileNameList = getProfileNameList(defaultProperties);
        }
        // if have env active, load env profile
        if (CollUtil.isNotEmpty(envProfileActive)) {
            for (String s : envProfileActive) {

                if (StrUtil.isNotBlank(s)) {
                    fileNameInit = fileNameInit + StringPool.DASH + s;
                }
                PropertySource<?> loadPropertySource = loadPropertySource(fileNameInit);
                if (loadPropertySource != null) {
                    System.out.println(SysLog.compact("find env config file：" + loadPropertySource.getName()));
                    allProperties.add(loadPropertySource);
                }
                Set<String> profileNameList = getProfileNameList(loadPropertySource);
                if (CollUtil.isNotEmpty(profileNameList)) {
                    for (String string : profileNameList) {
                        PropertySource<?> loadPropertySource2 = loadPropertySource(originFileNameInit + StringPool.DASH + string);
                        if (loadPropertySource2 != null) {
                            System.out.println(SysLog.compact("find env config file：" + loadPropertySource2.getName()));
                            allProperties.add(loadPropertySource2);
                        }
                    }
                }
            }
        } else {
            // if no env  active, load default profile
            for (String s1 : defaultProfileNameList) {
                PropertySource<?> loadPropertySource2 = loadPropertySource(originFileNameInit + StringPool.DASH + s1);
                if (loadPropertySource2 != null) {
                    System.out.println(SysLog.compact("find default config file：" + defaultProperties.getName()));

                    allProperties.add(loadPropertySource2);
                }
            }
        }

        // Set<String> sysNames = new HashSet<>();
        Map<String, Object> mapProperties = Maps.newConcurrentMap();

        // collect all properties
        allProperties.forEach(e2 -> {
            Properties properties = PropertySourceConverter.toProperties(e2);
            Set<Object> objects = properties.keySet();
            for (Object object : objects) {
                String key = object.toString();
                String v = properties.getProperty(key);
                String prefix = SysConstant.PARAM_PREFIX + SP.DOT;
                if (StrUtil.startWith(key, prefix) && ObjectUtil.isEmpty(mapProperties.get(key))) {
                    String iv = getInitParameterValue(key);
                    if (StrUtil.isNotBlank(iv)) {
                        // fix: need resolve placeholders
                        mapProperties.put(key, configEnvironment.resolvePlaceholders(iv));
                        return;
                    }
                    // fix: need resolve placeholders
                    mapProperties.put(key, configEnvironment.resolvePlaceholders(v));
                }
            }
        });

        // unnecessary parameters need not be converted here during early loading
        if (!mapProperties.isEmpty()) {
            BootStrapSpecialVsResolve bootStrapSpecialVsResolve = new BootStrapSpecialVsResolve();
            bootStrapSpecialVsResolve.handler(mapProperties, null);
            System.out.println(SysLog.compact("final " + mapProperties.size() + " properties will be inject env"));
            // fix: if enable redis server.port...attrs confusion
            if (propertySources.contains(Easy4j.EJ_SYS_ANNOTATION_PROPERTIES)) {
                propertySources.addBefore(Easy4j.EJ_SYS_ANNOTATION_PROPERTIES, new MapPropertySource(FIRST_ENV_NAME, mapProperties));
            } else {
                propertySources.addLast(new MapPropertySource(FIRST_ENV_NAME, mapProperties));
            }
        }
    }

    // 先找绝对路径再找类路径
    private static PropertySource<?> loadPropertySource(String fileNameInit) {

        PropertySource<?> loadPropertySource = null;

        try {
            String jarDir = JarPathUtil.getJarDirectory();
            if (StrUtil.isNotBlank(jarDir)) {
                // 先检查当前目录 在检查 config目录
                for (String path_ : ListTs.asList("", "config")) {
                    // 检查 properties yml yaml 文件
                    for (String suffix : ListTs.asList(SP.PROPERTIES_SUFFIX, SP.YML_SUFFIX, SP.YAML_SUFFIX)) {
                        String fileName = fileNameInit + suffix;
                        Path path = null;
                        if (StrUtil.isEmpty(path_)) {
                            path = Paths.get(jarDir, fileName);
                        } else {
                            path = Paths.get(jarDir, path_, fileName);
                        }
                        File file = new File(path.toUri());
                        String filePath = file.getAbsolutePath();
                        if (file.exists()) {
                            System.out.println(SysLog.compact("external configuration file found " + String.join(File.separator, ListTs.asList(path_, fileName))));
                            FileSystemResource fResource = new FileSystemResource(file);
                            if (SP.PROPERTIES_SUFFIX.equals(suffix)) {
                                Properties properties = PropertiesLoaderUtils.loadProperties(fResource);
                                loadPropertySource = new PropertiesPropertySource(filePath, properties);
                            } else if (SP.YML_SUFFIX.equals(suffix)) {
                                YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
                                loadPropertySource = loader.load(filePath, fResource).get(0);
                            } else if (SP.YAML_SUFFIX.equals(suffix)) {
                                YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
                                loadPropertySource = loader.load(filePath, fResource).get(0);
                            }
                            if (loadPropertySource != null) {
                                return loadPropertySource;
                            }
                        }
                    }
                }
            }

            String defaultFileNameWIthSuffix = fileNameInit + ".properties";
            ClassPathResource resource = new ClassPathResource(defaultFileNameWIthSuffix);
            if (resource.exists()) {
                Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                loadPropertySource = new PropertiesPropertySource("classpath:" + defaultFileNameWIthSuffix, properties);
                // propertySources.addLast(new PropertiesPropertySource("sca-early-config", properties));
            } else {
                String name = fileNameInit + ".yml";
                Resource resource1 = new ClassPathResource(name);
                if (!resource1.exists()) {
                    resource1 = new ClassPathResource(name);
                    if (!resource1.exists()) {
                        name = fileNameInit + ".yaml";
                        Resource resource3 = new ClassPathResource(name);
                        if (!resource3.exists()) {
                            return null;
                        }
                    }
                }
                YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
                loadPropertySource = loader.load("classpath:" + name, resource1).get(0);
            }
        } catch (IOException e) {
            throw new RuntimeException("加载 配置文件 失败", e);
        }
        return loadPropertySource;
    }


    /**
     * 判断是否使用了 sca 体系
     *
     * @return
     */
    public boolean isSca() {
        boolean isEnableSca = getEnvProperty(SysConstant.EASY4J_SCA_ENABLE, boolean.class);
        if (isEnableSca) {
            return true;
        } else {
            ClassLoader classLoader = this.getClass().getClassLoader();
            try {
                classLoader.loadClass("io.github.lbkones.config.nacos.ScaNacosEnvironmentFirst");
                return true;
            } catch (Exception ignored) {

            }
            try {
                classLoader.loadClass("easy4j.module.boot.sca.Enable");
                return true;
            } catch (ClassNotFoundException e) {
                try {
                    classLoader.loadClass("easy4j.boot.gateway.Enable");
                    return true;
                } catch (ClassNotFoundException ignored) {
                }
            }
            return false;
        }
    }

    /**
     * 这下面三个方法是为了兼容 环境对象使用的
     * 因为 Easy4j.getProperty()方法在EnvironmentPostProcessor调用链中可能有一定的滞后性（这个坑很大可能一时半会改不完所以最好的是遇到点改点），
     * 具体原因在 如果使用远程配置那么 EnvironmentPostProcessor回调传来的对象 ConfigurableEnvironment可能是新构造的，Easy4j.getProperty()这个使用的是老对象，
     * 如果这时候还使用 Easy4j.getProperty() 那么拿到的数据就会出现一定的滞后性
     * 其实这个问题不是什么大问题 因为SpringCloud数据最终会兼容合并 大部分数据不会丢，但是某个EnvironmentPostProcessor回调里面如果出了问题那么这个回调里本身要注入的数据可能就会丢失部分，导致配置变更了但是又没变
     *
     * @author bokun.li
     * @date 2025/6/25
     */
    public <T> T getEnvProperty(String name, Class<T> tClass) {
        return Easy4j.getEnvProperty(name, tClass, this.configEnvironment);
    }

    public EjSysProperties getEnvEjSysProperties() {
        return Easy4j.getEjSysPropertiesFromEnv(this.configEnvironment);
    }

    // 这个默认使用String类型
    public String getEnvProperty(String name) {
        return Easy4j.getEnvProperty(name, String.class, this.configEnvironment);
    }

    // 这个没有会报错
    public String getRequiredEnvProperty(String name) {
        return Easy4j.getRequiredEnvProperty(name, String.class, this.configEnvironment);
    }

}
