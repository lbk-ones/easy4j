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

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.event.NacosSauthServerRegisterEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;

@Slf4j
public class NacosEventListener implements DisposableBean {

    boolean enableServer = false;

    NamingService namingService = null;

    String serverName;

    String ipAddr;
    int serverPort;

    // 同步注册
    @EventListener
    public void listen1(NacosSauthServerRegisterEvent nacosSauthServerRegisterEvent) {
        try {
            long beginTime = System.currentTimeMillis();
            serverName = nacosSauthServerRegisterEvent.getServerName();
            boolean property = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
            boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_SERVER, boolean.class);
            Boolean registerEnable = Easy4j.getProperty(SysConstant.SPRING_REGISTER_TO_NACOS, Boolean.class,true);
            Boolean discoveryOrRegisterEnable = Easy4j.getProperty(SysConstant.SPRING_REGISTER_AND_DISCOVERY_NACOS, Boolean.class,true);
            enableServer = isServer && property;
            if (enableServer && (registerEnable && discoveryOrRegisterEnable)) {
                Easy4j.info(SysLog.compact("accept listen event,begin handler!" + serverName));
                NamingServerInvoker byEnv = NamingServerInvoker.createByEnv(null);
                namingService = byEnv.getNamingService();
                ipAddr = Easy4j.getProperty("spring.cloud.client.ip-address");
                Easy4j.info(SysLog.compact("get ip addr:" + ipAddr));
                serverPort = Easy4j.getProperty(SysConstant.EASY4J_SERVER_PORT, int.class);
                Easy4j.info(SysLog.compact("get server port:" + serverPort));

                Instance instance = new Instance();
                instance.setServiceName(serverName);
                instance.setIp(ipAddr);
                instance.setPort(serverPort);
                instance.setWeight(1.0);
                instance.setHealthy(true);
                instance.setEnabled(true);
                instance.setEphemeral(true);
                Easy4j.info(SysLog.compact("register info:" + instance));
                namingService.registerInstance(serverName, SysConstant.NACOS_AUTH_GROUP, instance);
                Easy4j.info(SysLog.compact("event handler success,cost " + (System.currentTimeMillis() - beginTime) + "ms"));
            }else{
                Easy4j.info(SysLog.compact("【"+serverName+"】skip register to nacos"));
            }
            Easy4j.info(SysLog.compact("auth feign direct url config is: feign."+serverName+ SP.DOT+SysConstant.NACOS_AUTH_GROUP+SP.DOT+SP.URL));
        } catch (Exception e) {
            log.error(SysLog.compact("event error!"), e);
        }


    }

    /**
     * 销毁注册
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        if (enableServer && namingService != null) {
            Easy4j.info(SysLog.compact("begin destroy nacos server:" + serverName + "@" + SysConstant.NACOS_AUTH_GROUP));
            long begintime = System.currentTimeMillis();
            namingService.deregisterInstance(serverName, SysConstant.NACOS_AUTH_GROUP, ipAddr, serverPort);
            Easy4j.info(SysLog.compact("destroy nacos server success! cost:" + (System.currentTimeMillis() - begintime) + "ms"));
        }
    }
}
