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
package io.github.lbkones.registry.nacos;

import cn.hutool.system.SystemUtil;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.properties.NacosPropetiesParse;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.base.starter.env.Easy4jEnvironmentFirst;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 注册中心参数初始化
 *
 * @see Easy4jEnvironmentFirst
 */
@Slf4j
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER - 1)
public class ScaNacosRegistryEnvironment extends AbstractEasy4jEnvironment {

    public static final String SCA_ENV = "sca-nacos-registry";

    @Override
    public String getName() {
        return SCA_ENV;
    }

    /**
     * 通过 Easy4jEnvironmentFirst 将数据加载进 EjSysProperties
     * 然后这里再将 EjSysProperties的数据转为 nacos 的配置
     *
     * @return
     */
    @Override
    public Properties getProperties() {
        if (!isSca()) return null;
        Properties properties = new Properties();
        EjSysProperties ejSys = Easy4j.getEjSysProperties();


        boolean forceRegister = getEnvProperty(SysConstant.EASY4J_FORCE_REGISTER_TO_REGISTRY, boolean.class);
        if (!SystemUtil.getOsInfo().isLinux() && !forceRegister) {
            System.out.println(SysLog.compact("It is detected that the current machine is running on the local machine, so it will not be registered to the registry. If registration is required, please change " + SysConstant.EASY4J_FORCE_REGISTER_TO_REGISTRY + " value to true"));
            setProperties(properties, SysConstant.SPRING_REGISTER_TO_NACOS, "false");
        }

        NacosPropetiesParse build = NacosPropetiesParse.build(this.getConfigEnvironment(), true);
        setProperties(properties, SysConstant.EASY4J_NACOS_GROUP, build.getNacosGroup());
        setProperties(properties, SysConstant.EASY4J_NACOS_NAMESPACE, build.getNacosNamespace());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_URL), build.getNacosUrl());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_USERNAME), build.getNacosUsername());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_PASSWORD), build.getNacosPassword());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_URL), build.getNacosDiscoveryUrl());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_USERNAME),  build.getNacosDiscoveryUsername());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_PASSWORD), build.getNacosDiscoveryPassword());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_GROUP), build.getNacosDiscoveryGroup());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_NAMESPACE), build.getNacosDiscoveryNameSpace());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_ENABLE), "true");
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        log.info(SysLog.compact(SCA_ENV + "已初始化。。"));

    }
}
