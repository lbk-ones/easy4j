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
package easy4j.module.nacosdubbo3;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.client.config.NacosConfigService;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.DefLog;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * config.app-config-file 当前应用的配置名称、
 * config.config-file 通用配置
 * config.group 组
 * config.namespace 命名空间
 * config.url nacos配置地址
 */
@Order(value = 12)
public class EnvironmentInit extends AbstractEasy4jEnvironment {


    private NacosConfigService nacosConfigService;

    public static final String APP_CONFIG_FILE_NAME = "nacos-app-config-file";
    public static final String CONFIG_NAME = "nacos-config-file";
    public static final String NACOS_HOST = "nacos-properties";

    @Override
    public String getName() {
        return APP_CONFIG_FILE_NAME;
    }

    @Override
    public Properties getProperties() {
        DefLog logger = getLogger();
        try {
            String appConfigFile = getProperty("config.app-config-file");
            Properties propertiesFromEnv = getPropertiesFromEnv(appConfigFile);
            this.nacosConfigService = new NacosConfigService(propertiesFromEnv);
            String group = propertiesFromEnv.getProperty("group");
            String serverStatus = this.nacosConfigService.getServerStatus();
            String appConfigInfo = nacosConfigService.getConfigAndSignListener(appConfigFile, group, 5000L, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {

                    MutablePropertySources propertySources = ((ConfigurableEnvironment) Easy4j.environment).getPropertySources();

                    if (StrUtil.isBlank(configInfo)) {
                        propertySources.remove(APP_CONFIG_FILE_NAME);
                        return;
                    }

                    Properties properties1 = new Properties();
                    StringReader stringReader = new StringReader(configInfo);
                    try {
                        properties1.load(stringReader);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (propertySources.contains(APP_CONFIG_FILE_NAME)) {
                        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(APP_CONFIG_FILE_NAME, properties1);
                        propertySources.replace(APP_CONFIG_FILE_NAME, propertiesPropertySource);
                    }
                }
            });

            Properties res = new Properties();
            if (StrUtil.isNotBlank(appConfigInfo)) {

                StringReader stringReader = new StringReader(appConfigInfo);
                try {
                    res.load(stringReader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }


            return res;
        } catch (Exception e) {
            //e.printStackTrace();
            logger.info("nacos参数覆盖出现异常" + e.getMessage());
            return null;
        }
    }

    public Properties getPropertiesFromEnv(String configFile) {

        String group = getProperty("config.group");
        String nameSpace = getProperty("config.namespace");
        String url = getProperty("config.url");
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, url);
        properties.put(PropertyKeyConst.NAMESPACE, nameSpace);
        properties.put("dataId", configFile);
        properties.put("group", group);
        properties.setProperty("connectTimeout", "10000"); // 连接超时时间
        properties.setProperty("retryTimes", "3"); // 设置重试次数
        properties.setProperty("retryInterval", "1000"); // 设置重试间隔时间，单位：毫秒
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            DefLog logger = getLogger();
            String configFile = getProperty("config.config-file");
            Properties propertiesFromEnv = getPropertiesFromEnv(configFile);
            String group = propertiesFromEnv.getProperty("group");
            logger.info("开始获取内容----->" + configFile);
            String configInfo = this.nacosConfigService.getConfigAndSignListener(configFile, group, 5000L, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    MutablePropertySources propertySources = ((ConfigurableEnvironment) Easy4j.environment).getPropertySources();

                    if (StrUtil.isBlank(configInfo)) {
                        propertySources.remove(CONFIG_NAME);
                        return;
                    }

                    Properties properties1 = new Properties();
                    StringReader stringReader = new StringReader(configInfo);
                    try {
                        properties1.load(stringReader);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    propertySources.replace(CONFIG_NAME, new PropertiesPropertySource(CONFIG_NAME, properties1));
                }
            });
            logger.info(":获取内容成功----->" + configInfo);
            Properties resConfig = new Properties();

            StringReader stringReader = new StringReader(configInfo);
            try {
                resConfig.load(stringReader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            MutablePropertySources propertySources = environment.getPropertySources();
            if (propertySources.contains(APP_CONFIG_FILE_NAME)) {
                propertySources.addAfter(APP_CONFIG_FILE_NAME, new PropertiesPropertySource(CONFIG_NAME, resConfig));
            } else {
                propertySources.addLast(new PropertiesPropertySource(CONFIG_NAME, resConfig));
            }
        } catch (Exception e) {

        }

    }


}
