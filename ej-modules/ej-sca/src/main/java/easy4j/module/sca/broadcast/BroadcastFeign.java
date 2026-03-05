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
package easy4j.module.sca.broadcast;

import feign.Client;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * BroadcastFeign
 * OpenFeign广播
 * 使用: @FeignClient(name = "xxx",configuration=BroadcastFeign.class)
 *
 * @author bokun.li
 * @date 2025/7/16
 */
@Configuration(
        proxyBeanMethods = false
)
public class BroadcastFeign {

    @Resource
    DiscoveryClient discoveryClient;

    @Bean
    public Client feignClient() {
        // 使用默认的 Feign Client 作为委托
        Client defaultClient = new Client.Default(null, null);
        return new BroadcastFeignClient(defaultClient, discoveryClient);
    }

}