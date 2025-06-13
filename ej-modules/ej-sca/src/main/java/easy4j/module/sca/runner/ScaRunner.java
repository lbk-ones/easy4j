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
package easy4j.module.sca.runner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.base.starter.env.Easy4jEnvironmentFirst;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.ThreadPoolUtils;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * ScaRunner
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class ScaRunner extends StandAbstractEasy4jResolve implements InitializingBean, CommandLineRunner, DisposableBean {
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
        // String serverName = ejSysProperties.getServerName();
        String ejSysPropertyName = Easy4j.getEjSysPropertyName(EjSysProperties::getServerName);
        String serverName = Easy4j.getRequiredProperty(ejSysPropertyName);
        String dataIds = getNormalDataIds(ejSysProperties);
        if (StrUtil.hasBlank(serverName, dataIds)) {
            log.info(SysLog.compact("server name or data id is null so listener is not enable :" + serverName + dataIds));
            return;
        }
        String group = ejSysProperties.getNacosConfigGroup();
        String nacosConfigFileExtension = ejSysProperties.getNacosConfigFileExtension();

        ConfigService configService = nacosConfigManager.getConfigService();
        for (String dataId : dataIds.split(StringPool.COMMA)) {
            String dataId1 = getDataId(dataId);
            String group1 = getGroup(dataId, group);
            String s = group1 + "@" + dataId1;
            log.info(SysLog.compact("please check! config listen in " + s));
            configService.addListener(dataId1, group1, new Listener() {
                @Override
                public Executor getExecutor() {
                    return ThreadPoolUtils.getThreadPoolTaskExecutor("sca-runner-listener-nacos-config", 2, 4, 10);
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info(SysLog.compact(s + "----> receiveConfigInfo ---->   " + configInfo));
                    try {
                        ConfigurableEnvironment environment = (ConfigurableEnvironment) Easy4j.environment;
                        MutablePropertySources propertySources = environment.getPropertySources();
                        PropertySource<?> propertySourceEnv = propertySources.get(AbstractEasy4jEnvironment.FIRST_ENV_NAME);
                        if (Objects.isNull(propertySourceEnv)) {
                            log.info(SysLog.compact("not get " + AbstractEasy4jEnvironment.FIRST_ENV_NAME + " from spring env"));
                            return;
                        }
                        String trim = StrUtil.trim(configInfo);
                        Map<String, @Nullable Object> map = Maps.newHashMap();
                        if (StrUtil.equals("properties", nacosConfigFileExtension)) {
                            Properties properties1 = new Properties();
                            StringReader stringReader = new StringReader(trim);
                            properties1.load(stringReader);
                            // only pick properties start with "easy4j."
                            for (Object o : properties1.keySet()) {
                                String str = Convert.toStr(o);
                                if (StrUtil.startWith(str, SysConstant.PARAM_PREFIX)) {
                                    map.put(str, properties1.get(str));
                                }
                            }

                        } else if (nacosConfigFileExtension != null && ("yml".endsWith(nacosConfigFileExtension) || "yaml".endsWith(nacosConfigFileExtension))) {
                            Resource byteArrayResource = new ByteArrayResource(trim.getBytes());
                            PropertySource<?> propertySource = new YamlPropertySourceLoader().load(Easy4jEnvironmentFirst.SCA_PROPERTIES_NAME, byteArrayResource).get(0);
                            Field[] fields = ReflectUtil.getFields(EjSysProperties.class);
                            Arrays.stream(fields).map(Easy4j::getEjSysPropertyName)
                                    .filter(Objects::nonNull)
                                    .forEach(e -> {
                                        Object source = propertySource.getProperty(e);
                                        if (ObjectUtil.isNotEmpty(source)) {
                                            map.put(e, source);
                                        }
                                    });
                        }
                        if (!map.isEmpty()) {
                            MapPropertySource propertiesPropertySource = new MapPropertySource(AbstractEasy4jEnvironment.FIRST_ENV_NAME, map);
                            propertySources.replace(AbstractEasy4jEnvironment.FIRST_ENV_NAME, propertiesPropertySource);
                        }
                    } catch (Exception e) {
                        log.error(SysLog.compact("nacos config receiveConfigInfo error--->"), e);
                    }


                }
            });
        }


        log.info(SysLog.compact("nacos config " + SysConstant.PARAM_PREFIX + " has been listen... "));
    }
}
