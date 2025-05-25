package easy4j.module.sca.runner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.google.common.collect.Maps;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.starter.Easy4jEnvironmentFirst;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import easy4j.module.base.utils.ThreadPoolUtils;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
public class ScaRunner implements InitializingBean, CommandLineRunner, DisposableBean {
    @Autowired
    NacosConfigManager nacosConfigManager;

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void run(String... args) throws Exception {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        String serverName = ejSysProperties.getServerName();
        String dataIds = ejSysProperties.getDataIds();
        if (StrUtil.hasBlank(serverName, dataIds)) {
            log.info(SysLog.compact("server name or data id is null so listener is not enable :" + serverName + dataIds));

            return;
        }
        String group = ejSysProperties.getNacosConfigGroup();
        String nacosConfigFileExtension = ejSysProperties.getNacosConfigFileExtension();

        ConfigService configService = nacosConfigManager.getConfigService();
        configService.addListener(dataIds, group, new Listener() {
            @Override
            public Executor getExecutor() {
                return ThreadPoolUtils.getThreadPoolTaskExecutor("sca-runner-listener-nacos-config", 2, 4, 10);
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info(SysLog.compact("receiveConfigInfo---->   " + configInfo));
                try {
                    ConfigurableEnvironment environment = (ConfigurableEnvironment) Easy4j.environment;
                    MutablePropertySources propertySources = environment.getPropertySources();
                    String trim = StrUtil.trim(configInfo);
                    Map<String, @Nullable Object> map = Maps.newHashMap();
                    if (StrUtil.equals("properties", nacosConfigFileExtension)) {
                        Properties properties1 = new Properties();
                        StringReader stringReader = new StringReader(trim);
                        properties1.load(stringReader);
                        // only pick properties start with "easy4j."
                        PropertySource<?> propertySource = propertySources.get(Easy4jEnvironmentFirst.SCA_PROPERTIES_NAME);
                        for (Object o : properties1.keySet()) {
                            String str = Convert.toStr(o);
                            if (StrUtil.startWith(str, SysConstant.PARAM_PREFIX)) {
                                Object property = propertySource.getProperty(str);
                                if (property == null) {
                                    map.put(str, properties1.get(str));
                                }
                            }
                        }
                    } else if ("yml".endsWith(nacosConfigFileExtension) || "yaml".endsWith(nacosConfigFileExtension)) {
                        Resource byteArrayResource = new ByteArrayResource(trim.getBytes());
                        PropertySource<?> propertySource = new YamlPropertySourceLoader().load(Easy4jEnvironmentFirst.SCA_PROPERTIES_NAME, byteArrayResource).get(0);
                        Field[] fields = ReflectUtil.getFields(EjSysProperties.class);
                        Arrays.stream(fields).filter(e -> {
                            int modifiers = e.getModifiers();
                            return !(Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers));
                        }).map(e -> {
                            String replace = StrUtil.replace(StrUtil.toUnderlineCase(e.getName()).toLowerCase(), StringPool.UNDERSCORE, StringPool.DASH);
                            return SysConstant.PARAM_PREFIX + StringPool.DOT + replace;
                        }).forEach(e -> {

                            Object source = propertySource.getProperty(e);
                            map.put(e, source);
                        });

                    }
                    if (!map.isEmpty()) {
                        MapPropertySource propertiesPropertySource = new MapPropertySource(Easy4jEnvironmentFirst.SCA_PROPERTIES_NAME, map);
                        propertySources.replace(Easy4jEnvironmentFirst.SCA_PROPERTIES_NAME, propertiesPropertySource);
                    }
                } catch (Exception e) {
                    log.error(SysLog.compact("nacos config receiveConfigInfo error--->"), e);
                }


            }
        });

        log.info(SysLog.compact("nacos config " + SysConstant.PARAM_PREFIX + " has been listen... "));
    }
}
