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
package io.github.lbkones.config.nacos;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.StrUtil;
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
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

/**
 * 晚于
 *
 * @see Easy4jEnvironmentFirst
 */
@Slf4j
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER - 2)
public class ScaNacosEnvironmentFirst extends AbstractEasy4jEnvironment {

    public static final String SCA_ENV = "sca-nacos-config-1";

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
        try{
            ClassPathResource resource = new ClassPathResource("nacos-version.txt");
            try (InputStream is = resource.getInputStream()) {
                System.out.println(SysLog.compact("current nacos_client "+new String(is.readAllBytes(), StandardCharsets.UTF_8)));
            }
        } catch (Exception ignored) {
        }

        Properties properties = new Properties();
        EjSysProperties ejSys = Easy4j.getEjSysProperties();
        NacosPropetiesParse build = NacosPropetiesParse.build(this.getConfigEnvironment(), true);
        List<NacosPropetiesParse.NacosDataId> dataIds = build.getDataIds();
        int i = 0;
        for (NacosPropetiesParse.NacosDataId dataId : dataIds) {
            String dataId_ = dataId.getDataId();
            String group = dataId.getGroup();
            if(StrUtil.isNotBlank(group)){
                dataId_ = dataId_+"?group="+group;
            }
            properties.setProperty(SysConstant.SPRING_CONFIG_IMPORT + "[" + i + "]", "optional:nacos:"+dataId_);
            i++;
        }
        setProperties(properties, SysConstant.EASY4J_NACOS_GROUP, build.getNacosGroup());
        setProperties(properties, SysConstant.EASY4J_NACOS_NAMESPACE, build.getNacosNamespace());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_URL), build.getNacosConfigUrl());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_USERNAME), build.getNacosConfigUsername());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_PASSWORD), build.getNacosConfigPassword());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_GOURP), build.getNacosConfigGroup());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_NAMESPACE), build.getNacosConfigNameSpace());
        // nacos 处理文件后缀
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_FILE_EXTENSION), build.getNacosConfigFileExtension());
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_ENABLE), "true");
        setPropertiesArr(properties, new String[]{"spring.nacos.config.enabled"}, "true");
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        log.info(SysLog.compact(SCA_ENV + "已初始化。。"));

    }
}
