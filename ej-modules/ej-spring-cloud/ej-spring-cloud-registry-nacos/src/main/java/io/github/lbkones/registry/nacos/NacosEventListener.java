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

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.event.NacosServicesRegisterEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听nacos注册事件，然后往nacos中注册服务
 * @since 2.1.4
 */
@Slf4j
public class NacosEventListener {


    private final static List<NacosServicesRegisterEvent> serviceList = new ArrayList<>();


    @Resource
    NacosServiceManager nacosServiceManager;

    // 同步注册
    @EventListener
    public void listen1(NacosServicesRegisterEvent nacosServicesRegisterEvent) {
        try {
            long beginTime = System.currentTimeMillis();
            String serverName = nacosServicesRegisterEvent.getServerName();
            String group = nacosServicesRegisterEvent.getGroup();
            Boolean registerEnable = Easy4j.getProperty(SysConstant.SPRING_REGISTER_TO_NACOS, Boolean.class, true);
            Boolean discoveryOrRegisterEnable = Easy4j.getProperty(SysConstant.SPRING_REGISTER_AND_DISCOVERY_NACOS, Boolean.class, true);
            if (registerEnable && discoveryOrRegisterEnable) {

                destroyed();

                Easy4j.info(SysLog.compact("accept listen event,begin handler!" + serverName));
                String ipAddr = Easy4j.getProperty("spring.cloud.client.ip-address");
                Easy4j.info(SysLog.compact("get ip addr:" + ipAddr));
                Integer port = nacosServicesRegisterEvent.getPort();
                Easy4j.info(SysLog.compact("get server port:" + port));
                nacosServicesRegisterEvent.setIpAddr(ipAddr);
                serviceList.add(nacosServicesRegisterEvent);
                Instance instance = new Instance();
                instance.setServiceName(serverName);
                instance.setIp(ipAddr);
                instance.setPort(port);
                instance.setWeight(1.0);
                instance.setHealthy(true);
                instance.setEnabled(true);
                instance.setEphemeral(true);
                Easy4j.info(SysLog.compact("register info:" + instance));
                nacosServiceManager.getNamingService().registerInstance(serverName, group, instance);
                Easy4j.info(SysLog.compact("event handler success,cost " + (System.currentTimeMillis() - beginTime) + "ms"));
            } else {
                Easy4j.info(SysLog.compact("【" + serverName + "】skip register to nacos"));
            }
            Easy4j.info(SysLog.compact(serverName + " feign direct url config is: feign." + serverName + SP.DOT + group + SP.DOT + SP.URL));
        } catch (Exception e) {
            log.error(SysLog.compact("event error!"), e);
        }


    }

    private void destroyed() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                NamingService namingService = nacosServiceManager.getNamingService();
                for (NacosServicesRegisterEvent servicesRegisterEvent : serviceList) {
                    namingService.deregisterInstance(servicesRegisterEvent.getServerName(), servicesRegisterEvent.getGroup(), servicesRegisterEvent.getIpAddr(), servicesRegisterEvent.getPort());
                }
            } catch (NacosException e) {
                log.error(SysLog.compact("nacos deregister service error"), e);
            }
        }));
    }
}
