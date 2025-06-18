package easy4j.module.sca.config;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import easy4j.infra.base.starter.env.Easy4j;
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
            enableServer = isServer && property;
            if (enableServer) {
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
                instance.setEphemeral(false);
                Easy4j.info(SysLog.compact("register info:" + instance));
                namingService.registerInstance(serverName, SysConstant.NACOS_AUTH_GROUP, instance);
                Easy4j.info(SysLog.compact("event handler success,cost " + (System.currentTimeMillis() - beginTime) + "ms"));
            }
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
