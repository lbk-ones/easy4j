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
import com.google.common.collect.Maps;
import easy4j.infra.base.resolve.BootStrapSpecialVsResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.base.starter.env.PropertySourceConverter;
import easy4j.infra.common.utils.SysLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.*;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 配置中心初始化 紧随 Easy4jEnvironmentFirst 之后执行
 * 兼容springcloud
 */
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER - 2)
public class Easy4jConfigEnvironment extends AbstractEasy4jEnvironment {
    private static boolean INIT_IS = false;
    public static final String EASY4j_ENV_NAME = "easy4j-config-environment";

    public static final String E_NAME_2 = "easy4j-cc-";
    private static final Map<CcSpi,String> hasStart = Maps.newConcurrentMap();

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
        if (!SpringCloudContextRefresher.hasSpringCloudEnvironment) {
            if (INIT_IS) return;
            INIT_IS = true;
        }
        CcSpi ccSpi = CcSpiFactory.get();
        if (!StrUtil.equals(DefaultCcSpi.DEFAULT_SPI, ccSpi.getName())) {
            System.out.println(SysLog.compact("begin load config from config center"));


            MutablePropertySources propertySources1 = environment.getPropertySources();
            PropertySource<?> propertySource = propertySources1.get(FIRST_ENV_NAME);
            Map<String, String> map = PropertySourceConverter.toMap(propertySource);
            System.out.println(SysLog.compact("print boot config keys: ↓"));
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value != null) {
                    System.out.println(SysLog.compact(key + "=" + configEnvironment.resolvePlaceholders(value)));
                }
            }
            // 只进行一次启动和监听
            String b = hasStart.get(ccSpi);
            boolean first = b == null;
            if(first){
                hasStart.put(ccSpi,"1");
                try {
                    ccSpi.start();
                    ccSpi.subscribe((key, properties) -> {
                        if (StrUtil.isBlank(key) || properties == null || properties.isEmpty()) return;
                        boolean refresh = SpringCloudContextRefresher.refresh();
                        // 没有springcloud环境需要手动刷新
                        if(!refresh){
                            ConfigurableEnvironment environment1 = (ConfigurableEnvironment) Easy4j.environment;
                            MutablePropertySources propertySources = environment1.getPropertySources();
                            String s = E_NAME_2 + key;
                            boolean contains = propertySources.contains(s);
                            if (contains) {
                                Map<String, Object> mapProperties = Maps.newHashMap();
                                for (String keyStr : properties.keySet()) {
                                    Object vp = properties.get(keyStr);
                                    if (null != vp) {
                                        String value = environment.resolvePlaceholders(vp.toString());
                                        mapProperties.put(keyStr, value);
                                    }
                                }
                                // fix: 这里也需要转换 不然和启动的时候参数对应不上
                                BootStrapSpecialVsResolve bootStrapSpecialVsResolve = new BootStrapSpecialVsResolve();
                                bootStrapSpecialVsResolve.handler(mapProperties, null);
                                MapPropertySource mapPropertySource = new MapPropertySource(s, properties);
                                propertySources.replace(s, mapPropertySource);
                            }
                        }
                    });
                    Runtime.getRuntime().addShutdownHook(new Thread(ccSpi::destroy));
                } catch (Exception e) {
                    System.err.println(SysLog.compact("config center start error " + e.getMessage()));
                    e.printStackTrace();
                    System.exit(1);
                    return;
                }
            }
            ccSpi.setBootParameters(map);
            // 从远程真正获取
            Map<String, Properties> config = ccSpi.getConfig();
            if (config != null && !config.isEmpty()) {
                Set<String> keySet = config.keySet();
                for (String key : keySet) {
                    if (StrUtil.isBlank(key)) continue;
                    String key_ = E_NAME_2 + key;
                    Properties bootConfig = config.get(key);
                    if (bootConfig != null && !bootConfig.isEmpty()) {
                        System.out.println(SysLog.compact("from config center ["+key+"] read "+ bootConfig.size()+" keys"));
                        Map<String, Object> mapProperties = Maps.newHashMap();
                        for (Object keyStr : bootConfig.keySet()) {
                            String keyS = keyStr.toString();
                            String property = bootConfig.getProperty(keyS);
                            String value = environment.resolvePlaceholders(property);
                            mapProperties.put(keyS, value);
                        }
                        // fix: 需要进行启动映射
                        BootStrapSpecialVsResolve bootStrapSpecialVsResolve = new BootStrapSpecialVsResolve();
                        bootStrapSpecialVsResolve.handler(mapProperties, null);
                        MutablePropertySources propertySources = environment.getPropertySources();
                        MapPropertySource propertiesPropertySource = new MapPropertySource(key_, mapProperties);
                        // fix: 优先级必须比注解要高
                        if (propertySources.contains(Easy4j.EJ_SYS_ANNOTATION_PROPERTIES) && propertySources.contains(FIRST_ENV_NAME)) {
                            propertySources.addBefore(Easy4j.EJ_SYS_ANNOTATION_PROPERTIES, propertiesPropertySource);
                        } else {
                            propertySources
                                    .addAfter(FIRST_ENV_NAME, propertiesPropertySource);
                        }
                    }
                }
            }else{
                if(first){
                    System.err.println(SysLog.compact("not found keys from config center，please check config!"));
                    System.exit(1);
                }
            }
        }
    }

}
