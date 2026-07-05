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

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.properties.NacosPropetiesParse;
import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.ThreadPoolUtils;
import io.github.lbkones.config.api.ConfigCenterFactory;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
        NacosPropetiesParse build = NacosPropetiesParse.build(null, true);
        List<NacosPropetiesParse.NacosDataId> dataIds = build.getDataIds();
        ConfigService configService = nacosConfigManager.getConfigService();
        for (NacosPropetiesParse.NacosDataId dataId_ : dataIds){
            String group = dataId_.getGroup();
            String dataId = dataId_.getDataId();
            String s = group + "@" + dataId;
            log.info(SysLog.compact("please check! config listen in " + s));
            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return ThreadPoolUtils.getThreadPoolTaskExecutor("nacos-listen", 1, 4, 10);
                }

                @Override
                public void receiveConfigInfo(String configInfo) {

                    log.info(SysLog.compact(s + "----> receiveConfigInfo ---->   " + configInfo));

                    String trim = StrUtil.trim(configInfo);
                    StringReader stringReader = new StringReader(trim);
                    Properties properties = new Properties();
                    try {
                        properties.load(stringReader);
                        Map<@Nullable String, @Nullable String> res = Maps.newHashMap();
                        for (Object o : properties.keySet()) {
                            Object o1 = properties.get(o);
                            res.put(String.valueOf(o), String.valueOf(o1));
                        }
                        ConfigCenterFactory.get().change(res);
                    } catch (IOException ignored) {

                    }

                }
            });
        }
        log.info(SysLog.compact("nacos config " + SysConstant.PARAM_PREFIX + " has been listen... "));
    }
}
