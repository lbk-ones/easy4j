package easy4j.module.sentinel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import lombok.Getter;

public class InitConfig {
    @Getter
    private static  Boolean init = false;

    public static void init(){
        synchronized (InitConfig.class){
            if(init) return;
            init = true;
            // 初始化 Sentinel 核心
            InitExecutor.doInit();
            // 配置 Dashboard 地址（如果配置了）
            Boolean enabled = Easy4j.getProperty(SysConstant.EASY4J_SENTINEL_DASHBOARD_ENABLE, Boolean.class, false);
            String url = Easy4j.getProperty(SysConstant.EASY4J_SENTINEL_DASHBOARD_CONSOLE_URL, String.class,"");
            Integer runtimePort = Easy4j.getProperty(SysConstant.EASY4J_SENTINEL_DASHBOARD_RUNTIME_PORT, Integer.class, 8179);
            if(enabled && runtimePort>0 && StrUtil.isNotBlank(url)){
                TransportConfig.setRuntimePort(runtimePort);
                SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER,url);
                SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER,url);
                SentinelConfig.setConfig(SentinelConfig.PROJECT_NAME_PROP_KEY,Easy4j.getProperty(SysConstant.SPRING_SERVER_NAME));
            }
        }

    }
}
