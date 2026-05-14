package io.github.lbkones.config.httpnacos;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.properties.cc.ConfigCenterFactory;
import easy4j.infra.base.resolve.NacosUrlResolve;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.base.starter.env.PropertySourceConverter;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import io.github.lbkones.config.api.AbstractCcSpi;
import io.github.lbkones.config.api.ConfigChange;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 使用这个有弊端
 * 只能变更spring环境中的参数，不能变更 @Value 和注入Properties参数对象的值
 * 通过 Easy4j.getProperties("xxx")
 * 和
 */
@Slf4j
public class CcHttpNacosSpi extends AbstractCcSpi {

    CompatibleNacosHttpClient client;

    List<String> dataId;
    String group;
    String namespace;

    @Override
    public Map<String,Properties> getConfig() {
        try {
            Map<String,Properties> m = new HashMap<>();

            for (String did : dataId) {
                String dataId1 = getDataId(did);
                String group1 = getGroup(did, group);
                String config = client.getConfig(dataId1, group1);
                Properties properties1 = getProperties(did, config);
                m.put(dataId1+SP.AT+group1,properties1);
            }
            return m;
        } catch (Exception e) {
            log.error("get config error ->" ,e);
            return null;
        }
    }

    @Override
    public void start() {
        try {
            EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
            String nacosUrl_ = ejSysProperties.getNacosUrl();
            String nacosConfigUrl = ejSysProperties.getNacosConfigUrl();
            String dataIds = ejSysProperties.getDataIds();
            group = StrUtil.blankToDefault(ejSysProperties.getNacosConfigGroup(), ejSysProperties.getNacosGroup());
            dataId = ListTs.splitToList(dataIds,SP.COMMA);
            String nacosUsername = StrUtil.blankToDefault(ejSysProperties.getNacosConfigUsername(),ejSysProperties.getNacosUsername());
            String password = StrUtil.blankToDefault(ejSysProperties.getNacosConfigPassword(),ejSysProperties.getNacosPassword());
            namespace = ejSysProperties.getNacosNameSpace();
            String nacosUrl = StrUtil.blankToDefault(nacosConfigUrl, nacosUrl_);
            NacosUrlResolve nacosUrlResolve = new NacosUrlResolve();
            Map<String, String> stringStringMap = new HashMap<>();
            nacosUrlResolve.handler(stringStringMap, nacosUrl);
            String nurl = stringStringMap.get(SysConstant.EASY4J_SCA_NACOS_URL);

            client = new CompatibleNacosHttpClient(nurl, namespace);
            ConfigChange change = this.configChange;
            for (String s : dataId) {
                String dataId1 = getDataId(s);
                String group1 = getGroup(s, group);
                client.addListener(dataId1, group1, (dataId, group, configInfo) -> {
                    Map<@Nullable String, @Nullable Object> res = Maps.newHashMap();
                    Properties properties = getProperties(dataId, configInfo);
                    for (Object o : properties.keySet()) {
                        Object o1 = properties.get(o);
                        res.put(String.valueOf(o), String.valueOf(o1));
                    }
                    if (change != null) {
                        change.change(dataId+SP.AT+group,res);
                    }
                    Map<String, String> mr = new HashMap<>();
                    for (String key : res.keySet()) {
                        Object o = res.get(key);
                        if (o != null) {
                            mr.put(key, o.toString());
                        }
                    }
                    ConfigCenterFactory.get().change(mr);
                });
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties getProperties(String dataId, String configInfo) {
        Properties properties = new Properties();
        String trim = StrUtil.trim(configInfo);

        if (StrUtil.endWith(dataId, SP.PROPERTIES_SUFFIX)) {
            StringReader stringReader = new StringReader(trim);
            try {
                properties.load(stringReader);

            } catch (IOException ignored) {

            }
        } else if (StrUtil.endWith(dataId, SP.YAML_SUFFIX) || StrUtil.endWith(dataId, SP.YML_SUFFIX)) {
            YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
            ByteArrayResource byteArrayResource = new ByteArrayResource(trim.getBytes(StandardCharsets.UTF_8));
            try {
                List<PropertySource<?>> load = yamlPropertySourceLoader.load(SP.DOUBLE_DASH, byteArrayResource);
                PropertySource<?> propertySource = ListTs.get(load, 0);
                properties = PropertySourceConverter.toProperties(propertySource);
            } catch (IOException ignored) {
            }
        }
        return properties;
    }

    @Override
    public void destroy() {
        try {
            client.shutdown();
        } catch (Exception ignored) {

        }
    }

    @Override
    public String getName() {
        return "nacos-http";
    }


}
