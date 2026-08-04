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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysFieldInfo;
import easy4j.infra.base.properties.NacosPropetiesParse;
import easy4j.infra.base.resolve.BootStrapSpecialVsResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.config.StringConfigToPropertySourceUtils;
import  io.github.lbkones.config.api.ConfigCenterFactory;
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
import java.util.Set;

/**
 * 晚于
 * 启动的时候重载远程加载的数据
 *
 * @see ConfigDataEnvironmentPostProcessor
 */
@Slf4j
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER + 1)
public class ScaNacosEnvironmentTwo extends AbstractEasy4jEnvironment {

    public static final String SYS_OVERIDE_PARAMS = "sca-nacos-environment-sys-overide";
    public static final String NOSYS_OVERIDE_PARAMS = "sca-nacos-environment-nosys-overide";

    @Override
    public String getName() {
        return SYS_OVERIDE_PARAMS;
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
     */
    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        if (getEnvProperty("spring.cloud.nacos.config.enabled", Boolean.class,true) == false) {
            System.out.println(SysLog.compact("skip load nacos config two step"));
            return;
        }
        NacosPropetiesParse build = NacosPropetiesParse.build(this.getConfigEnvironment(), true);
        List<NacosPropetiesParse.NacosDataId> dataIds = build.getDataIds();
        for (NacosPropetiesParse.NacosDataId dataId_ : dataIds) {
            String group = dataId_.getGroup();
            String dataId = dataId_.getDataId();
            String nacosPropertiesResourceName = group + "@" + dataId;
            System.out.println(SysLog.compact("begin override nacos init remote config：" + nacosPropertiesResourceName));
            MutablePropertySources propertySources = environment.getPropertySources();
            PropertySource<?> propertySource = propertySources.get(nacosPropertiesResourceName);
            if (ObjectUtil.isEmpty(propertySource)) {
                System.err.println(SysLog.compact("nacos configuration center failed to read the value. " + nacosPropertiesResourceName));
                continue;
            }
            assert propertySource != null;

            Map<String, String> ccMap = castToMap(propertySource);
            // init config center
            ConfigCenterFactory.get().change(ccMap);

            Map<String, Object> mapPropertiesResource = Maps.newHashMap();
            Map<String, Object> nonSysMap = Maps.newHashMap();
            for (Map.Entry<String, String> ccEntry : ccMap.entrySet()) {
                String key = ccEntry.getKey();
                String value = ccEntry.getValue();
                if(StrUtil.startWith(key, SysConstant.PARAM_PREFIX+ SP.DOT)){
                    mapPropertiesResource.put(key, value);
                }else{
                    nonSysMap.put(key,value);
                }
            }

            // 以 easy4j.开头的参数 优先级比较高 如果同时设置 那么会采用前者的值
            if (CollUtil.isNotEmpty(mapPropertiesResource)) {

                BootStrapSpecialVsResolve bootStrapSpecialVsResolve = new BootStrapSpecialVsResolve();
                bootStrapSpecialVsResolve.handler(mapPropertiesResource, null);

                System.out.println(SysLog.compact("success override nacos sys config keys:" + mapPropertiesResource.size()));
                OriginTrackedMapPropertySource originTrackedMapPropertySource = new OriginTrackedMapPropertySource(SYS_OVERIDE_PARAMS, mapPropertiesResource, true);
                propertySources.addBefore(FIRST_ENV_NAME, originTrackedMapPropertySource);
            }
            // SYS_OVERIDE_PARAMS > NOSYS_OVERIDE_PARAMS > FIRST_ENV_NAME
            // 覆盖所有非系统参数
            if (CollUtil.isNotEmpty(nonSysMap)) {
                System.out.println(SysLog.compact("success override nacos nonsys config keys:" + nonSysMap.size()));
                if(propertySources.contains(SYS_OVERIDE_PARAMS)){
                    OriginTrackedMapPropertySource originTrackedMapPropertySource = new OriginTrackedMapPropertySource(NOSYS_OVERIDE_PARAMS, nonSysMap, true);
                    propertySources.addAfter(SYS_OVERIDE_PARAMS, originTrackedMapPropertySource);
                }else{
                    OriginTrackedMapPropertySource originTrackedMapPropertySource = new OriginTrackedMapPropertySource(NOSYS_OVERIDE_PARAMS, nonSysMap, true);
                    propertySources.addBefore(FIRST_ENV_NAME, originTrackedMapPropertySource);
                }
            }
        }

    }
    private static Map<String, String> castToMap(PropertySource<?> propertySource){
        return StringConfigToPropertySourceUtils.toMapStr(propertySource);
    }

    private static Map<String, Object> getNoSysMap(PropertySource<?> propertySource, Map<String, EjSysFieldInfo> stringEjSysFieldInfoMap) {
        Map<String, Object> nonSysMap = Maps.newHashMap();
        Object source = propertySource.getSource();
        if (source instanceof Map<?, ?> source1) {
            Set<? extends Map.Entry<?, ?>> entries = source1.entrySet();
            for (Map.Entry<?, ?> entry : entries) {
                String key = String.valueOf(entry.getKey());
                EjSysFieldInfo ejSysFieldInfo = stringEjSysFieldInfoMap.get(key);
                if (null == ejSysFieldInfo) {
                    Object value = entry.getValue();
                    if (ObjectUtil.isNotEmpty(value)) nonSysMap.put(key, value);
                }
            }
        }
        return nonSysMap;
    }
}
