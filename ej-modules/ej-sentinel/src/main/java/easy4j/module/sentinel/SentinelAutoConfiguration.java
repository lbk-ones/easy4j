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
package easy4j.module.sentinel;

import com.alibaba.csp.sentinel.init.InitExecutor;
import easy4j.module.sentinel.annotation.FlowDegradeAspect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * SentinelAutoConfiguration
 *
 * @author bokun.li
 * @date 2025-05
 */
@Configuration
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelAutoConfiguration implements InitializingBean {

    private final SentinelProperties sentinelProperties;

    public SentinelAutoConfiguration(SentinelProperties sentinelProperties) {
        this.sentinelProperties = sentinelProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化 Sentinel 核心
        InitExecutor.doInit();
        // 配置 Dashboard 地址（如果配置了）
//        if (sentinelProperties.getDashboardServer() != null) {
//            TransportConfig.setRuntimePort(sentinelProperties.getDashboardServer());
//        }
//        FlowRuleManager.loadRules();

        // 设置应用名称
//        System.setProperty("project.name", sentinelProperties.getAppName());
//
//        // 如果配置了 eager=true，立即连接 Dashboard
//        if (sentinelProperties.isEager()) {
//            // 这里可以添加连接 Dashboard 的逻辑（可选）
//        }

    }



    // 支持 @FlowDegrade 注解的切面
    @Bean
    @ConditionalOnMissingBean
    public FlowDegradeAspect sentinelResourceAspect() {
        return new FlowDegradeAspect();
    }
}
