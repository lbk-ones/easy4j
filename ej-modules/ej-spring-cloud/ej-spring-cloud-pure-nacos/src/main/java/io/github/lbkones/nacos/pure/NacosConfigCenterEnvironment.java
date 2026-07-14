package io.github.lbkones.nacos.pure;

import cn.hutool.core.thread.NamedThreadFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.MD5Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Order(value = ConfigDataEnvironmentPostProcessor.ORDER + 1)
public class NacosConfigCenterEnvironment implements EnvironmentPostProcessor {

    public static ConfigService configService;
    private final Map<String, Object> lastKeyMap = new ConcurrentHashMap<>();

    private Properties buildNacosProperties(NacosStandProperties parse) {
        Properties properties = new Properties();
        properties.put("serverAddr", parse.getNacosConfigServerAddr());
        if (StringUtils.hasText(parse.getNacosConfigNamespace())) {
            properties.put("namespace", parse.getNacosConfigNamespace());
        }
        if (StringUtils.hasText(parse.getNacosConfigUsername())) {
            properties.put("username", parse.getNacosConfigUsername());
        }
        if (StringUtils.hasText(parse.getNacosConfigPassword())) {
            properties.put("password", parse.getNacosConfigPassword());
        }
        properties.put("connectTimeout", 3000);
        properties.put("readTimeout", 5000);
        return properties;
    }

    public boolean hasBlankStr(String... text) {
        for (String s : text) {
            if (!StringUtils.hasText(s)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        NacosStandProperties parse = NacosStandProperties.parse(environment, application);
        if (parse == null || !parse.isConfigEnabled()) return;
        log(false, "begin load keys form nacos config center!");

        String addr = parse.getNacosConfigServerAddr();
        String userName = parse.getNacosConfigUsername();
        String nacosPassword = parse.getNacosConfigPassword();
        String configNamespace = parse.getNacosConfigNamespace();
        String nacosConfigGroup = parse.getNacosConfigGroup();
        List<NacosStandProperties.DataId> dataIds = parse.getDataIds();
        if (CollectionUtils.isEmpty(dataIds)) {
            log(true, "because dataids is empty so skip config parse!!!");
            return;
        }
        if (hasBlankStr(addr, userName, nacosPassword, configNamespace, nacosConfigGroup)) {
            log(true, "skip config parse!!!");
            return;
        }
        try {
            ClassPathResource resource = new ClassPathResource("nacos-version.txt");
            try (InputStream is = new BufferedInputStream(resource.getInputStream())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                byte[] bytes = buffer.toByteArray();
                log(false, "current nacos client " + new String(bytes, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }
        try {
            if (configService == null) {
                Properties properties = buildNacosProperties(parse);
                configService = NacosFactory.createConfigService(properties);
            }
            boolean exit = true;
            for (NacosStandProperties.DataId dataId : dataIds) {
                String dataId_ = dataId.getDataId();
                String group = dataId.getGroup();
                log(false, "fetch  【" + dataId_ + "】 group 【" + group + "】 properties");
                long l = System.currentTimeMillis();
                String config = configService.getConfig(dataId_, group, 5000);
                long l1 = System.currentTimeMillis();
                log(false, "fetch cost " + (l1 - l) + "ms");

                if (!StringUtils.hasText(config)) {
                    log(true, "Please add the configuration 【" + dataId_ + "】 in the nacos configuration center，group is " + group);
                    continue;
                } else {
                    exit = false;
                }
                final String resourceName = group + "@" + dataId_;
                PropertySource<?> propertySource = SCPropertySourceUtils.autoParse(resourceName, config);
                MutablePropertySources propertySources = environment.getPropertySources();
                propertySources.addLast(propertySource);

                setLastKey(propertySource, null);

                log(false, "begin listener " + resourceName);
                configService.addListener(dataId_, group, new Listener() {


                    @Override
                    public Executor getExecutor() {
                        return Executors.newSingleThreadExecutor(new NamedThreadFactory("ncl-", true));
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        log(false, "nacos client receive config ===> " + configInfo);
                        String trim = configInfo.trim();
                        if (!StringUtils.hasText(trim)) return;

                        Set<String> keys = new HashSet<>();
                        PropertySource<?> propertySource = SCPropertySourceUtils.autoParse(resourceName, configInfo);
                        MutablePropertySources propertySources = environment.getPropertySources();
                        if (propertySources.contains(resourceName)) {
                            propertySources.replace(resourceName, propertySource);
                            log(false, "nacos environment has been replace");
                        } else {
                            propertySources.addLast(propertySource);
                            log(false, "nacos environment has been add last");
                        }
                        setLastKey(propertySource, keys);
                        CloudPropertiesRefresh cloudPropertiesRefresh = CloudPropertiesRefreshHolder.cloudPropertiesRefresh;
                        if (cloudPropertiesRefresh != null) {
                            log(false, "begin notify spring cloud context ,the keys size is " + keys.size());
                            long beginTime = System.currentTimeMillis();
                            cloudPropertiesRefresh.sendRefreshEvent(keys);
                            long endTime = System.currentTimeMillis();
                            log(false, "notify cost " + (endTime - beginTime) + "ms");
                        }
                    }
                });
            }
            if (exit) {
                log(true, "Please add the configuration in the nacos configuration center，");
                System.exit(1);
            }
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }


    }

    private void setLastKey(PropertySource<?> propertySource, Set<String> keys) {
        Map<String, Object> map = SCPropertySourceUtils.toMap(propertySource);
        log(false, "get " + map.size() + " keys ");
        for (Map.Entry<String, Object> newEntry : map.entrySet()) {
            String key = newEntry.getKey();
            Object value = newEntry.getValue();
            try {
                // 不处理字符串null
                String newValue = MD5Utils.md5Hex(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
                String oldValue = String.valueOf(lastKeyMap.get(key));
                if (!newValue.equals(oldValue)) {
                    lastKeyMap.put(key, newValue);
                    if (keys != null) {
                        keys.add(key);
                    }
                }
            } catch (NoSuchAlgorithmException ignored) {
            }
        }
    }

    public void log(boolean error, String msg) {
        if (error) {
            System.err.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + " [ERROR] " + msg);
        } else {
            System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + " [INFO] " + msg);
        }
    }

}
