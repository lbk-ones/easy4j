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
package io.github.lbkones.config.api;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.base.starter.env.PropertySourceConverter;
import easy4j.infra.common.utils.SysLog;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.*;

import java.util.Map;
import java.util.Properties;

/**
 * 配置中心初始化 紧随 Easy4jEnvironmentFirst 之后执行
 */
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER - 2)
public class Easy4jConfigEnvironment extends AbstractEasy4jEnvironment {
    private static boolean INIT_IS = false;
    public static final String EASY4j_ENV_NAME = "easy4j-config-environment";

    public static final String E_NAME_2 = "easy4j-cc-";

    @Override
    public String getName() {
        return EASY4j_ENV_NAME;
    }

    @Override
    public Properties getProperties() {
        return null;
    }


    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        initEnv(environment, application);
        if (isSca()) return;
        // 如果没有使用sca架构的话这里只会执行一次
        // 从这里加载远程配置文件
        // 不允许二次执行
        if (INIT_IS) return;
        INIT_IS = true;
        CcSpi ccSpi = CcSpiFactory.get();
        if (!StrUtil.equals(DefaultCcSpi.DEFAULT_SPI, ccSpi.getName())) {
            System.out.println(SysLog.compact("begin load config from config center"));


            try {
                ccSpi.start();
            } catch (Exception e) {
                System.err.println(SysLog.compact("config center start error " + e.getMessage()));
                return;
            }

            MutablePropertySources propertySources1 = environment.getPropertySources();
            PropertySource<?> propertySource = propertySources1.get(FIRST_ENV_NAME);
            Map<String, String> map = PropertySourceConverter.toMap(propertySource);
            ccSpi.setBootParameters(map);

            ccSpi.subscribe((key, properties) -> {
                if (StrUtil.isBlank(key) || properties == null || properties.isEmpty()) return;
                ConfigurableEnvironment environment1 = (ConfigurableEnvironment) Easy4j.environment;
                MutablePropertySources propertySources = environment1.getPropertySources();
                String s = E_NAME_2 + key;
                boolean contains = propertySources.contains(s);
                if(contains){
                    MapPropertySource mapPropertySource = new MapPropertySource(s, properties);
                    propertySources.replace(s, mapPropertySource);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(ccSpi::destroy));

            Map<String, Properties> config = ccSpi.getConfig();
            if (config != null && !config.isEmpty()) {
                for (String key : config.keySet()) {
                    if (StrUtil.isBlank(key)) continue;
                    String key_ = E_NAME_2 + key;
                    Properties bootConfig = config.get(key);
                    if (bootConfig != null && !bootConfig.isEmpty()) {
                        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(key_, bootConfig);
                        environment.getPropertySources()
                                .addAfter(FIRST_ENV_NAME, propertiesPropertySource);
                    }
                }
            }
        }
    }

}
