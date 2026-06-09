package io.github.lbkones.config.httpnacos;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.NacosPropetiesParse;
import easy4j.infra.base.starter.env.PropertySourceConverter;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysLog;
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
 * Nacos HTTP配置中心SPI实现
 * <br/>
 * 通过HTTP接口访问Nacos配置中心，支持Nacos 2.x和3.x版本
 * <br/>
 * 支持认证方式：
 * 1. 用户名密码认证：通过login接口获取accessToken
 * 2. AccessKey/SecretKey签名认证
 * <br/>
 * 只能通过 Easy4j.getProperties("xxx") 或者 SpringUtil.getProperty("xxx")
 * <br/>
 */
@Slf4j
public class CcHttpNacosSpi extends AbstractCcSpi {

    CompatibleNacosHttpClient client;

    NacosPropetiesParse nacosPropetiesParse;


    @Override
    public Map<String, Properties> getConfig() {
        try {
            Map<String, Properties> m = new HashMap<>();
            List<NacosPropetiesParse.NacosDataId> dataIds = nacosPropetiesParse.getDataIds();
            for (NacosPropetiesParse.NacosDataId dataId : dataIds) {
                String dataId1 = dataId.getDataId();
                String group1 = dataId.getGroup();
                String dg = dataId1 + SP.AT + group1;
                String config = client.getConfig(dataId1, group1);
                Properties properties1 = getProperties(dataId1, config);
                m.put(dg, properties1);
            }
            return m;
        } catch (Exception e) {
            log.error("get config error ->", e);
            return null;
        }
    }

    @Override
    public void start() {
        try {
            nacosPropetiesParse = NacosPropetiesParse.build(null, true);
            String username = nacosPropetiesParse.getNacosConfigUsername();
            String password = nacosPropetiesParse.getNacosConfigPassword();
            String nurl = nacosPropetiesParse.getNacosConfigUrl();
            String nameSpace = nacosPropetiesParse.getNacosConfigNameSpace();
            if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
                System.out.println(SysLog.compact("Initializing Nacos HTTP client with username/password authentication"));
                client = new CompatibleNacosHttpClient(nurl, nameSpace, username, password);
            } else {
                System.out.println(SysLog.compact("Initializing Nacos HTTP client without authentication"));
                client = new CompatibleNacosHttpClient(nurl, nameSpace);
            }

            List<NacosPropetiesParse.NacosDataId> dataIds = nacosPropetiesParse.getDataIds();
            for (NacosPropetiesParse.NacosDataId dataId_ : dataIds) {
                String dataId1 = dataId_.getDataId();
                String group1 = dataId_.getGroup();
                client.addListener(dataId1, group1, new CompatibleNacosHttpClient.ConfigListener() {

                    private String lastMd5 = null;

                    @Override
                    public void onConfigChange(String dataId, String group, String configInfo) {
                        String md5 = DigestUtil.md5Hex(configInfo, StandardCharsets.UTF_8);
                        System.out.println(SysLog.compact("The value in the configuration center has changed " + dataId + "@" + group) + " last md5 is " + lastMd5 + " current md5 is " + md5);
                        if (StrUtil.isNotBlank(configInfo)) {
                            if (StrUtil.equals(lastMd5, md5)) return;
                            ConfigChange change = CcHttpNacosSpi.this.configChange;
                            Map<@Nullable String, @Nullable Object> res = Maps.newHashMap();
                            Properties properties = getProperties(dataId, configInfo);
                            for (Object o : properties.keySet()) {
                                Object o1 = properties.get(o);
                                res.put(String.valueOf(o), String.valueOf(o1));
                            }
                            if (change != null) {
                                change.change(dataId + SP.AT + group, res);
                            }
                            lastMd5 = md5;
                        }

                    }
                });
            }

        } catch (Exception e) {
            log.error("Failed to initialize Nacos HTTP ConfigCenter", e);
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
            if (client != null) {
                client.shutdown();
                log.info("Nacos HTTP client shutdown successfully");
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    public String getName() {
        return "nacos-http";
    }

}
