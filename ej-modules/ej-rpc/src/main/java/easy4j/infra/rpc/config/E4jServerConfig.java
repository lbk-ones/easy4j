package easy4j.infra.rpc.config;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import lombok.Data;

@Data
public class E4jServerConfig {

    /**
     * 是否禁用服务端,禁用之后不再向外提供服务
     */
    private boolean disabled = false;

    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 要监听的端口
     */
    private Integer port;

    /**
     * 是否允许端口复用（解决端口占用 “TIME_WAIT” 状态导致的启动失败）
     */
    private boolean soReuseAddr = true;

    /**
     * 服务端半连接大小
     */
    private Integer soBackLog = 2048;

    /**
     * 基础权重
     */
    private Integer weight = 1;

    /**
     * 心跳上报间隔时间，默认5秒
     */
    private Long heartInfoReportFixRateMilli = 5 * 1000L;

    /**
     * 心跳死亡时间
     */
    private Long heartDeathFixRateMilli;

    public String getServerName() {
        if(StrUtil.isBlank(serverName)){
            return IntegratedFactory.getRpcConfig().get("spring.application.name");
        }
        return serverName;
    }

}