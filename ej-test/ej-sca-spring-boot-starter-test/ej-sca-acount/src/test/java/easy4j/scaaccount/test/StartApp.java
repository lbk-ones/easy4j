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
package easy4j.scaaccount.test;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import easy4j.module.base.starter.Easy4JStarter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@Easy4JStarter(
        serverPort = 100010,
        serverName = "test-account",
        serviceDesc = "测试",
        author = "bokun.li",
        enableH2 = true,
        h2Url = "jdbc:h2:mem:testaccount"
        // 使用h2当数据库
)
/**
 * StartApp
 *
 * @author bokun.li
 * @date 2025-05
 */
@EnableDiscoveryClient
@SpringBootTest
public class StartApp {

    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    NacosConfigManager nacosConfigManager;

    @Test
    void testDiscoveryClient() {
        ConfigService configService = nacosConfigManager.getConfigService();

        List<ServiceInstance> instances = discoveryClient.getInstances("my-service");
        for (ServiceInstance instance : instances) {
            String host = instance.getHost() + ":" + instance.getPort();
            System.out.println(host);
        }
    }
}
