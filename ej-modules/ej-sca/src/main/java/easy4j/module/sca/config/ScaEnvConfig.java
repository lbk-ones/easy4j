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
package easy4j.module.sca.config;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.resolve.NacosUrlResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.base.starter.env.Easy4jEnvironmentFirst;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.Properties;

/**
 * 晚于
 *
 * @see Easy4jEnvironmentFirst
 */
@Slf4j
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER - 1)
public class ScaEnvConfig extends AbstractEasy4jEnvironment {

    public static final String SCA_ENV = "sca-env-config";

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
        String ejSysPropertyName = Easy4j.getEjSysPropertyName(EjSysProperties::getServerName);
        String serverName = Easy4j.getRequiredProperty(ejSysPropertyName);
        if (StrUtil.isBlank(serverName)) {
            log.info(SysLog.compact(SysConstant.EASY4J_SERVER_NAME + "is not set,so skip sca config setting...."));
            return null;
        }
        String dataIds = ejSys.getDataIds();
        String env = ejSys.getEnv();
        String nacosGroup = StrUtil.blankToDefault(ejSys.getNacosGroup(), "DEFAULT_GROUP");
        String nacosGroup2 = ejSys.getNacosConfigGroup();
        boolean nacosConfigStrict = ejSys.isNacosConfigStrict();
        String nacosConfigFileExtension = StrUtil.blankToDefault(ejSys.getNacosConfigFileExtension(), "properties");
        // 手动指定dataId
        if (StrUtil.isNotBlank(dataIds)) {
            List<String> list1 = ListTs.asList(dataIds.split(StringPool.COMMA));
            List<String> dataids = ListTs.newArrayList();
            for (int i = 0; i < list1.size(); i++) {
                String e = list1.get(i);

                String dataId = getDataId(e);
                String _nacosConfigFileExtension = StrUtil.blankToDefault(StrUtil.subSuf(dataId, dataId.lastIndexOf(SP.DOT) + 1), nacosConfigFileExtension);
                String group = getGroup(e, null);
                String configImport = "nacos:";
                if (!nacosConfigStrict) {
                    configImport = "optional:" + configImport;
                }
                String fDataId = "";
                String suffix = StrUtil.endWith(dataId, StringPool.DOT + _nacosConfigFileExtension) ? "" : StringPool.DOT + _nacosConfigFileExtension;
                if (StrUtil.isNotBlank(env)) {
                    // 无组要加后缀
                    String s = dataId + StringPool.DASH + env + suffix;
                    // 有组则原样返回
                    if (StrUtil.isNotBlank(group)) {
                        s = e;
                    }
                    dataids.add(s);
                    fDataId = s;
                    configImport += s;
                } else {
                    if (StrUtil.isBlank(group)) {
                        String s = dataId + suffix;
                        fDataId = s;
                        dataids.add(s);
                        configImport += s;
                    } else {
                        fDataId = e;
                        dataids.add(e);
                        configImport += e;
                    }
                }
                System.out.println(SysLog.compact("the data-ids identified are: " + fDataId));
                properties.setProperty(SysConstant.SPRING_CONFIG_IMPORT + "[" + i + "]", configImport);
            }
            properties.setProperty(SysConstant.EASY4J_NACOS_DATA_IDS, String.join(StringPool.COMMA, dataids));
        } else {
            // 如果没指定那么就以 serverName 为准进行推算
            String dataids = "nacos:";
            if (!nacosConfigStrict) {
                dataids = "optional:" + dataids;
            }
            if (StrUtil.isNotBlank(env)) {
                dataids += serverName + StringPool.DASH + env + StringPool.DOT + nacosConfigFileExtension;
            } else {
                dataids += serverName + StringPool.DOT + nacosConfigFileExtension;
            }
            properties.setProperty(SysConstant.SPRING_CONFIG_IMPORT + "[0]", dataids);
            List<String> list = ListTs.asList(dataids.split(StringPool.COLON));
            String s = ListTs.get(list, list.size() - 1, "");
            System.out.println(SysLog.compact("the data-ids identified are: " + s));

            properties.setProperty(SysConstant.EASY4J_NACOS_DATA_IDS, s);
        }

        String nacosUrl = ejSys.getNacosUrl();
        if (StrUtil.isBlank(nacosUrl)) {
            throw new EasyException("请设置参数：" + Easy4j.getEjSysPropertyName(EjSysProperties::getNacosUrl));
        }
        NacosUrlResolve nacosUrlResolve = new NacosUrlResolve();
        String username = nacosUrlResolve.getUsername(nacosUrl);
        String password = nacosUrlResolve.getPassword(nacosUrl);
        nacosUrlResolve.handler(properties, nacosUrl);


        String nacosNameSpace = StrUtil.blankToDefault(ejSys.getNacosNameSpace(), "public");
        String nacosConfigUrl = ejSys.getNacosConfigUrl();
        setProperties(properties, SysConstant.EASY4J_NACOS_GROUP, nacosGroup);
        setProperties(properties, SysConstant.EASY4J_NACOS_NAMESPACE, nacosNameSpace);
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_URL), nacosConfigUrl);
        String nacosConfigUsername = ejSys.getNacosConfigUsername();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_USERNAME), nacosConfigUsername);
        String configPassword = ejSys.getNacosConfigPassword();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_PASSWORD), configPassword);
        String configGroup = ejSys.getNacosConfigGroup();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_GOURP), StrUtil.blankToDefault(configGroup, nacosGroup));
        String configNamespace = ejSys.getNacosConfigNamespace();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_NAMESPACE), StrUtil.blankToDefault(configNamespace, nacosNameSpace));
        String nacosDiscoveryUrl = ejSys.getNacosDiscoveryUrl();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_URL), nacosDiscoveryUrl);
        String discoveryUsername = ejSys.getNacosDiscoveryUsername();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_USERNAME), StrUtil.blankToDefault(discoveryUsername, username));
        String discoveryPassword = ejSys.getNacosDiscoveryPassword();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_PASSWORD), StrUtil.blankToDefault(discoveryPassword, password));
        String discoveryGroup = ejSys.getNacosDiscoveryGroup();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_GROUP), StrUtil.blankToDefault(discoveryGroup, nacosGroup));
        String discoveryNamespace = ejSys.getNacosDiscoveryNamespace();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_NAMESPACE), StrUtil.blankToDefault(discoveryNamespace, nacosNameSpace));
        System.out.println(SysLog.compact("Nacos 2.2.0 uses gRPC to establish long connections by default. The initial connection may be slow. Please wait...."));
        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        log.info(SysLog.compact(SCA_ENV + "已初始化。。"));

    }
}
