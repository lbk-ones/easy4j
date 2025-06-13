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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysFieldInfo;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.resolve.BootStrapSpecialVsResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysLog;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 晚于
 * 启动的时候重载远程加载的数据
 *
 * @see ConfigDataEnvironmentPostProcessor
 */
@Slf4j
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER + 1)
public class OverrideNacosInitConfig extends AbstractEasy4jEnvironment {

    public static final String SCA_OVERRIDE_ENV = "sca-env-overide-nacos-config";

    @Override
    public String getName() {
        return SCA_OVERRIDE_ENV;
    }

    @Override
    public Properties getProperties() {
        return null;
    }


    /**
     * 走到这里来说明 nacos配置基本没问题 参数正常获取
     * <p>
     * 系统参数和spring参数有一层对照
     * 参数的解析是分散再各个服务解析的 比如数据库，nacos等
     * 但是有些特殊的参数是只有从配置中提前读取然后再转换才生效了的具体转换逻辑在，如果把所有的参数都丢到nacos配置中心的话 那么那些参数是没有经过转化的
     *
     * @param environment
     * @param application
     */
    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        List<EjSysFieldInfo> allEjSysFieldInfoList = EjSysFieldInfo.getAllEjSysInfoList();
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        String dataIds = getNormalDataIds(ejSysProperties);
        for (String dataId : ListTs.asList(dataIds.split(StringPool.COMMA))) {
            String nacosGroup = getGroup(dataId, ejSysProperties.getNacosGroup());
            String dataIds2 = getDataId(dataId);
            String nacosPropertiesResourceName = nacosGroup + "@" + dataIds2;
            System.out.println(SysLog.compact("begin override nacos init remote config：" + nacosPropertiesResourceName));
            MutablePropertySources propertySources = environment.getPropertySources();
            PropertySource<?> propertySource = propertySources.get(nacosPropertiesResourceName);
            if (ObjectUtil.isEmpty(propertySource)) {
                System.err.println(SysLog.compact("nacos configuration center failed to read the value. " + nacosPropertiesResourceName));
                return;
            }
            Map<String, Object> mapPropertiesResource = Maps.newHashMap();
            allEjSysFieldInfoList.forEach(ejSysFieldInfo -> {
                String sysConstantName = ejSysFieldInfo.getSysConstantName();
                assert propertySource != null;
                Object property = propertySource.getProperty(sysConstantName);
                if (ObjectUtil.isNotEmpty(property)) {
                    mapPropertiesResource.put(sysConstantName, property);
                }
            });

            if (CollUtil.isNotEmpty(mapPropertiesResource)) {

                BootStrapSpecialVsResolve bootStrapSpecialVsResolve = new BootStrapSpecialVsResolve();
                bootStrapSpecialVsResolve.handler(mapPropertiesResource, null);

                System.out.println(SysLog.compact("success override nacos config keys:" + mapPropertiesResource.keySet().size()));
                OriginTrackedMapPropertySource originTrackedMapPropertySource = new OriginTrackedMapPropertySource(getName(), mapPropertiesResource, true);

                propertySources.addAfter(FIRST_ENV_NAME, originTrackedMapPropertySource);
            }
        }

    }
}
