package io.github.lbkones.nacos.v2x;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.properties.NacosPropetiesParse;
import easy4j.infra.base.starter.env.PropertySourceConverter;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysLog;
import io.github.lbkones.config.api.AbstractCcSpi;
import io.github.lbkones.config.api.ConfigChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Nacos 2.x 客户端配置中心实现
 * <br/>
 * 使用原生Nacos 2.x客户端SDK，支持完整的配置管理和监听功能
 * <br/>
 * 只能通过 Easy4j.getProperties("xxx") 或者 SpringUtil.getProperty("xxx")
 * <br/>
 *
 * @author libokun
 */
@Slf4j
public class CcNacosClientV2xSpi extends AbstractCcSpi {

    private ConfigService configService;
    private final Map<String, Properties> configCache = new ConcurrentHashMap<>();
    NacosPropetiesParse nacosPropetiesParse;
    @Override
    public Map<String, Properties> getConfig() {
        if (configService == null) {
            log.warn("ConfigService not initialized, call start() first");
            return null;
        }

        try {
            Map<String, Properties> result = new HashMap<>();
            List<NacosPropetiesParse.NacosDataId> dataIds = nacosPropetiesParse.getDataIds();
            for (NacosPropetiesParse.NacosDataId dataId_ : dataIds) {
                String dataId = dataId_.getDataId();
                String group = dataId_.getGroup();
                String dg = dataId + SP.AT + group;
                Properties properties1 = configCache.get(dg);
                if (properties1 != null) {
                    result.put(dg, properties1);
                    continue;
                }
                String content = configService.getConfig(dataId, group, 5000);
                if (StrUtil.isNotBlank(content)) {
                    configCache.put(dg, parseConfig(dataId, content));
                    Properties properties = parseConfig(dataId, content);
                    result.put(dg, properties);
                }
            }

            return result;
        } catch (NacosException e) {
            log.error("Failed to get config from Nacos 2.x", e);
            return null;
        }
    }

    @Override
    public void start() {
        try {
            nacosPropetiesParse = NacosPropetiesParse.build(null, true);
            String nacosConfigUrl = nacosPropetiesParse.getNacosConfigUrl();
            String nameSpace = nacosPropetiesParse.getNacosConfigNameSpace();
            Properties properties = buildNacosProperties(nacosConfigUrl, nameSpace);
            configService = NacosFactory.createConfigService(properties);
            List<NacosPropetiesParse.NacosDataId> dataIds = nacosPropetiesParse.getDataIds();
            for (NacosPropetiesParse.NacosDataId dataId_ : dataIds) {
                final String dataId = dataId_.getDataId();
                final String group = dataId_.getGroup();
                final String dg = dataId + SP.AT + group;
                String content = configService.getConfig(dataId, group, 5000);
                if (StrUtil.isNotBlank(content)) {
                    configCache.put(dg, parseConfig(dataId, content));
                }
                configService.addListener(dataId, group, new Listener() {
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        configInfo = StrUtil.trim(configInfo);
                        if (StrUtil.isBlank(configInfo)) {
                            return;
                        }
                        System.out.println(SysLog.compact("The value in the configuration center has changed " +dg));


                        Properties props = parseConfig(dataId, configInfo);
                        configCache.put(dg, props);

                        Map<String, Object> res = new HashMap<>();
                        for (Object key : props.keySet()) {
                            Object value = props.get(key);
                            if (value != null) {
                                res.put(String.valueOf(key), String.valueOf(value));
                            }
                        }
                        ConfigChange change = CcNacosClientV2xSpi.this.configChange;
                        if (change != null) {
                            change.change(dg, res);
                        }
                    }

                    @Override
                    public Executor getExecutor() {
                        return Executors.newSingleThreadExecutor(new NamedThreadFactory("nacos-config-listener",true));
                    }

                });
            }
        } catch (NacosException e) {
            log.error("Failed to initialize Nacos 2.x ConfigService", e);
            throw new RuntimeException("Nacos 2.x ConfigService initialization failed", e);
        }
    }

    @Override
    public void destroy() {
        if (configService != null) {
            try {
                configService.shutDown();
            } catch (NacosException ignored) {
            }
            log.info(SysLog.compact("Nacos 2.x ConfigService shutdown requested"));
        }
    }

    @Override
    public String getName() {
        return "nacos-client-v2x";
    }

    private Properties buildNacosProperties(String serverAddr, String namespace) {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);

        if (StrUtil.isNotBlank(namespace)) {
            properties.put("namespace", namespace);
        }

        String username =nacosPropetiesParse.getNacosConfigUsername();
        String password = nacosPropetiesParse.getNacosConfigPassword();

        if (StrUtil.isNotBlank(username)) {
            properties.put("username", username);
        }
        if (StrUtil.isNotBlank(password)) {
            properties.put("password", password);
        }

        properties.put("connectTimeout", 3000);
        properties.put("readTimeout", 5000);

        return properties;
    }

    private String getServerAddr(EjSysProperties ejSysProperties) {
        String nacosConfigUrl = ejSysProperties.getNacosConfigUrl();
        String nacosUrl = ejSysProperties.getNacosUrl();

        if (StrUtil.isNotBlank(nacosConfigUrl)) {
            return nacosConfigUrl;
        }

        if (StrUtil.isNotBlank(nacosUrl)) {
            return nacosUrl;
        }

        throw new IllegalArgumentException("Nacos server address not configured");
    }

    private Properties parseConfig(String dataId, String content) {
        Properties properties = new Properties();
        String trim = StrUtil.trim(content);

        if (StrUtil.endWith(dataId, SP.PROPERTIES_SUFFIX)) {
            try {
                properties.load(new StringReader(trim));
            } catch (IOException e) {
                log.warn("Failed to parse properties config: {}", dataId, e);
            }
        } else if (StrUtil.endWith(dataId, SP.YAML_SUFFIX) || StrUtil.endWith(dataId, SP.YML_SUFFIX)) {
            YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
            ByteArrayResource byteArrayResource = new ByteArrayResource(trim.getBytes(StandardCharsets.UTF_8), dataId);
            try {
                List<PropertySource<?>> load = yamlPropertySourceLoader.load(dataId, byteArrayResource);
                PropertySource<?> propertySource = ListTs.get(load, 0);
                properties = PropertySourceConverter.toProperties(propertySource);
            } catch (IOException e) {
                log.warn("Failed to parse yaml config: {}", dataId, e);
            }
        } else {
            properties.put("content", trim);
        }

        return properties;
    }


}
